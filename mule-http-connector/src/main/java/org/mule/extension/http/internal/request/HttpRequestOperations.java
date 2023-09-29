/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Optional.of;
import static org.mule.extension.http.internal.HttpConnectorConstants.CONNECTOR_OVERRIDES;
import static org.mule.extension.http.internal.HttpConnectorConstants.HTTP_ENABLE_PROFILING;
import static org.mule.extension.http.internal.HttpConnectorConstants.REQUEST;
import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSE;
import static org.mule.extension.http.internal.request.UriUtils.buildPath;
import static org.mule.extension.http.internal.request.UriUtils.resolveUri;
import static org.mule.extension.http.internal.request.HttpRequestUtils.createHttpRequester;
import static org.mule.extension.http.internal.request.HttpRequestUtils.handleCursor;
import static org.mule.extension.http.internal.request.EmptyDistributedTraceContextManager.getDistributedTraceContextManager;
import static org.mule.sdk.api.annotation.param.MediaType.ANY;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.HttpMetadataResolver;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.annotation.Streaming;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.notification.Fires;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.notification.NotificationEmitter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.sdk.compatibility.api.utils.ForwardCompatibilityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class HttpRequestOperations implements Initialisable, Disposable {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestOperations.class);
  private static final int WAIT_FOR_EVER = MAX_VALUE;
  private SuccessStatusCodeValidator defaultStatusCodeValidator;
  private HttpRequesterRequestBuilder defaultRequestBuilder;
  private HttpRequester httpRequester;

  @Inject
  private MuleContext muleContext;
  @Inject
  private TransformationService transformationService;
  @Inject
  private SchedulerService schedulerService;
  @Inject
  @Named("http.request.fixedHeadersRegistry")
  private HashMap<String, List<String>> injectedHeaders;

  @Inject
  private java.util.Optional<ForwardCompatibilityHelper> forwardCompatibilityHelper;

  private Scheduler scheduler;

  private boolean httpResponseProfilingEnabled;

  /**
   * Consumes an HTTP service.
   *
   * @param uriSettings                URI settings parameter group
   * @param method                     The HTTP method for the request.
   * @param overrides                  configuration overrides parameter group
   * @param responseValidationSettings response validation parameter group
   * @param requestBuilder             configures the request
   * @param client                     the http connection
   * @param config                     the configuration for this operation. All parameters not configured will be taken from it.
   * @param correlationInfo            the current message's correlation info
   * @param callback                   the non-blocking completion callback
   * @return an {@link Result} with {@link HttpResponseAttributes}
   */
  @Summary("Executes a HTTP Request")
  @OutputResolver(output = HttpMetadataResolver.class)
  @Fires(RequestNotificationActionProvider.class)
  @Throws(RequestErrorTypeProvider.class)
  @Streaming
  @MediaType(value = ANY, strict = false)
  public void request(@Placement(order = 1) @ParameterGroup(name = "URI Settings") UriSettings uriSettings,
                      @Placement(order = 2) @Optional(defaultValue = "GET") String method,
                      @ParameterGroup(name = CONNECTOR_OVERRIDES) ConfigurationOverrides overrides,
                      @Placement(order = 3) @ParameterGroup(name = REQUEST) HttpRequesterRequestBuilder requestBuilder,
                      @ParameterGroup(name = RESPONSE) ResponseValidationSettings responseValidationSettings,
                      @Connection HttpExtensionClient client,
                      @Config HttpRequesterConfig config,
                      CorrelationInfo correlationInfo,
                      NotificationEmitter notificationEmitter,
                      StreamingHelper streamingHelper,
                      CompletionCallback<InputStream, HttpResponseAttributes> callback) {
    try {
      HttpRequesterRequestBuilder resolvedBuilder = requestBuilder != null ? requestBuilder : defaultRequestBuilder;

      handleCursor(resolvedBuilder);

      resolvedBuilder.setCorrelationInfo(correlationInfo);

      String resolvedUri;
      if (uriSettings.getUrl() == null) {
        UriParameters uriParameters = client.getDefaultUriParameters();
        String resolvedBasePath = config.getBasePath();
        String resolvedPath = resolvedBuilder.replaceUriParams(buildPath(resolvedBasePath, uriSettings.getPath()));
        resolvedUri =
            resolveUri(uriParameters.getScheme(), uriParameters.getHost().trim(), uriParameters.getPort(), resolvedPath);
      } else {
        resolvedUri = resolvedBuilder.replaceUriParams(uriSettings.getUrl());
      }

      ResponseValidator responseValidator = responseValidationSettings.getResponseValidator();
      responseValidator = responseValidator != null ? responseValidator : defaultStatusCodeValidator;

      LOGGER.debug("Sending '{}' request to '{}'.", method, resolvedUri);
      httpRequester.doRequest(client, config, resolvedUri, method, overrides.getRequestStreamingMode(),
                              overrides.getSendBodyMode(), overrides.getFollowRedirects(), client.getDefaultAuthentication(),
                              overrides.getResponseTimeout(), responseValidator, transformationService,
                              getRequestCreator(resolvedBuilder), true, muleContext, scheduler, notificationEmitter,
                              streamingHelper, callback, injectedHeaders, forwardCompatibilityHelper
                                  .map(fcHelper -> fcHelper.getDistributedTraceContextManager(correlationInfo))
                                  .orElse(getDistributedTraceContextManager()));
    } catch (Throwable t) {
      callback.error(t instanceof Exception ? (Exception) t : new DefaultMuleException(t));
    }
  }

  private RequestCreator getRequestCreator(HttpRequesterRequestBuilder builder) {
    return new RequestCreator() {

      @Override
      public HttpRequestBuilder createRequestBuilder(HttpRequesterConfig config) {
        return builder.toHttpRequestBuilder(config);
      }

      @Override
      public TypedValue<?> getBody() {
        return builder.getBody();
      }

      @Override
      public java.util.Optional<CorrelationData> getCorrelationData() {
        return of(new CorrelationData(builder.getCorrelationInfo(), builder.getSendCorrelationId(), builder.getCorrelationId()));
      }
    };
  }

  @Override
  public void initialise() throws InitialisationException {
    defaultStatusCodeValidator = new SuccessStatusCodeValidator("0..399");
    defaultRequestBuilder = new HttpRequesterRequestBuilder();
    // Profiling API is only available with this system property
    httpResponseProfilingEnabled = getBoolean(HTTP_ENABLE_PROFILING);
    initializeHttpRequester();

    this.scheduler = schedulerService.ioScheduler();
  }

  private void initializeHttpRequester() throws InitialisationException {
    try {
      httpRequester = createHttpRequester(httpResponseProfilingEnabled, muleContext);
    } catch (MuleException e) {
      throw new InitialisationException(e, this);
    }
  }

  @Override
  public void dispose() {
    if (this.scheduler != null) {
      scheduler.shutdownNow();
    }
  }

  public static class Builder {

    private MuleContext muleContext;
    private TransformationService transformationService;
    private SchedulerService schedulerService;
    private HashMap<String, List<String>> injectedHeaders;

    private Builder() {}

    public static Builder newInstance() {
      return new Builder();
    }

    public Builder withMuleContext(MuleContext muleContext) {
      this.muleContext = muleContext;
      return this;
    }

    public Builder withTransformationService(TransformationService transformationService) {
      this.transformationService = transformationService;
      return this;
    }

    public Builder withSchedulerService(SchedulerService schedulerService) {
      this.schedulerService = schedulerService;
      return this;
    }

    public Builder withInjectedHeaders(HashMap<String, List<String>> injectedHeaders) {
      this.injectedHeaders = injectedHeaders;
      return this;
    }

    public HttpRequestOperations build() {
      if (this.muleContext == null) {
        throw new IllegalStateException("muleContext must be set");
      }
      if (this.transformationService == null) {
        throw new IllegalStateException("transformationService must be set");
      }
      if (this.schedulerService == null) {
        throw new IllegalStateException("schedulerService must be set");
      }
      HttpRequestOperations httpRequestOperations = new HttpRequestOperations();
      httpRequestOperations.muleContext = this.muleContext;
      httpRequestOperations.transformationService = this.transformationService;
      httpRequestOperations.schedulerService = this.schedulerService;
      httpRequestOperations.injectedHeaders = injectedHeaders;
      return httpRequestOperations;
    }

  }
}
