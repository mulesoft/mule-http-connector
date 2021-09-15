/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.MAX_VALUE;
import static org.mule.extension.http.internal.HttpConnectorConstants.CONNECTOR_OVERRIDES;
import static org.mule.extension.http.internal.HttpConnectorConstants.HTTP_ENABLE_PROFILING;
import static org.mule.extension.http.internal.HttpConnectorConstants.REQUEST;
import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSE;
import static org.mule.runtime.core.api.config.MuleManifest.getProductVersion;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpErrorMessageGenerator;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.HttpMetadataResolver;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.extension.http.internal.request.profiling.HttpRequestResponseProfilingDataProducerAdaptor;
import org.mule.extension.http.internal.request.profiling.HttpProfilingServiceAdaptor;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
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
import org.mule.runtime.http.api.HttpConstants;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestOperations implements Initialisable, Disposable {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestOperations.class);
  private static final int WAIT_FOR_EVER = MAX_VALUE;
  private static final Map<Character, String> RESERVED_CONVERSION;
  private static final MuleVersion runtimeVersion = new MuleVersion(getProductVersion());

  // We are not currently depending on Guava to be able to use ImmutableMap
  static {
    Map<Character, String> map = new HashMap<>();
    map.put(' ', "%20");
    // RFC-3986: delims
    map.put(':', "%3A");
    map.put('#', "%24");
    map.put('[', "%5B");
    map.put(']', "%5D");
    map.put('@', "%40");
    // RFC-3986: sub-delims
    map.put('!', "%21");
    map.put('$', "%24");
    map.put('\'', "%27");
    map.put('(', "%28");
    map.put(')', "%29");
    map.put('+', "%2B");
    map.put(',', "%2C");
    map.put(';', "%3B");
    RESERVED_CONVERSION = map;
  }

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

      int resolvedTimeout = resolveResponseTimeout(overrides.getResponseTimeout());
      ResponseValidator responseValidator = responseValidationSettings.getResponseValidator();
      responseValidator = responseValidator != null ? responseValidator : defaultStatusCodeValidator;

      LOGGER.debug("Sending '{}' request to '{}'.", method, resolvedUri);
      httpRequester.doRequest(client, config, resolvedUri, method, overrides.getRequestStreamingMode(),
                              overrides.getSendBodyMode(),
                              overrides.getFollowRedirects(), client.getDefaultAuthentication(), resolvedTimeout,
                              responseValidator,
                              transformationService, resolvedBuilder, true, muleContext, scheduler, notificationEmitter,
                              streamingHelper, callback, injectedHeaders, correlationInfo.getCorrelationId());
    } catch (Throwable t) {
      callback.error(t instanceof Exception ? (Exception) t : new DefaultMuleException(t));
    }
  }

  /**
   * If the body is a {@link Cursor}, we need to change it for the {@link CursorProvider} to re-read the content in the case we
   * need to make a retry of a request.
   */
  protected void handleCursor(HttpRequesterRequestBuilder resolvedBuilder) {
    if (resolvedBuilder.getBody().getValue() instanceof CursorStream) {
      CursorStream cursor = (CursorStream) (resolvedBuilder.getBody().getValue());

      long position = cursor.getPosition();
      CursorStreamProvider provider = (CursorStreamProvider) cursor.getProvider();

      if (position == 0) {
        resolvedBuilder.setBody(new TypedValue<Object>(provider, resolvedBuilder.getBody().getDataType(),
                                                       resolvedBuilder.getBody().getByteLength()));
      } else {
        resolvedBuilder.setBody(new TypedValue<Object>(new OffsetCursorProviderWrapper(provider, position),
                                                       resolvedBuilder.getBody().getDataType(),
                                                       resolvedBuilder.getBody().getByteLength()));
      }
    }
  }

  private static String encodeReservedCharacters(String path) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < path.length(); i++) {
      char c = path.charAt(i);
      if (RESERVED_CONVERSION.containsKey(c)) {
        builder.append(RESERVED_CONVERSION.get(c));
      } else {
        builder.append(c);
      }
    }

    return builder.toString();
  }

  private String resolveUri(HttpConstants.Protocol scheme, String host, Integer port, String path) {
    // Encode spaces to generate a valid HTTP request.
    return scheme.getScheme() + "://" + host + ":" + port + encodeReservedCharacters(path);
  }

  private int resolveResponseTimeout(Integer responseTimeout) {
    if (muleContext.getConfiguration().isDisableTimeouts()) {
      return WAIT_FOR_EVER;
    } else {
      return responseTimeout != null ? responseTimeout : muleContext.getConfiguration().getDefaultResponseTimeout();
    }
  }

  protected String buildPath(String basePath, String path) {
    String resolvedBasePath = basePath;
    String resolvedRequestPath = path;

    if (!resolvedBasePath.startsWith("/")) {
      resolvedBasePath = "/" + resolvedBasePath;
    }

    if (resolvedBasePath.endsWith("/") && resolvedRequestPath.startsWith("/")) {
      resolvedBasePath = resolvedBasePath.substring(0, resolvedBasePath.length() - 1);
    }

    if (!resolvedBasePath.endsWith("/") && !resolvedRequestPath.startsWith("/") && !resolvedRequestPath.isEmpty()) {
      resolvedBasePath += "/";
    }

    return resolvedBasePath + resolvedRequestPath;
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
      httpRequester = new HttpRequester(new HttpRequestFactory(), new HttpResponseToResult(), new HttpErrorMessageGenerator(),
                                        getProfilingDataProducer());
    } catch (MuleException e) {
      throw new InitialisationException(e, this);
    }
  }

  private HttpRequestResponseProfilingDataProducerAdaptor getProfilingDataProducer() throws MuleException {
    if (!httpResponseProfilingEnabled) {
      return null;
    }

    HttpProfilingServiceAdaptor profilingServiceAdaptor = new HttpProfilingServiceAdaptor();

    // Manually inject the profiling service
    muleContext.getInjector().inject(profilingServiceAdaptor);

    return profilingServiceAdaptor.getProfilingHttpRequestDataProducer();
  }

  @Override
  public void dispose() {
    if (this.scheduler != null) {
      scheduler.shutdownNow();
    }
  }
}
