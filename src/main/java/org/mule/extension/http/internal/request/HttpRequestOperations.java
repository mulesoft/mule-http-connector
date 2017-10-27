/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static org.mule.extension.http.internal.HttpConnectorConstants.CONNECTOR_OVERRIDES;
import static org.mule.extension.http.internal.HttpConnectorConstants.REQUEST;
import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSE;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.encodeSpaces;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.HttpMetadataResolver;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.extension.api.annotation.Streaming;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.http.api.HttpConstants;

import java.io.InputStream;

import javax.inject.Inject;

public class HttpRequestOperations implements Initialisable, Disposable {

  private static final int WAIT_FOR_EVER = MAX_VALUE;

  @Inject
  private MuleContext muleContext;
  @Inject
  private SchedulerService schedulerService;

  private Scheduler scheduler;

  /**
   * Consumes an HTTP service.
   *
   * @param uriSettings URI settings parameter group
   * @param method The HTTP method for the request.
   * @param overrides configuration overrides parameter group
   * @param responseValidationSettings response validation parameter group
   * @param requestBuilder configures the request
   * @param client the http connection
   * @param config the configuration for this operation. All parameters not configured will be taken from it.
   * @return an {@link Result} with {@link HttpResponseAttributes}
   */
  @Summary("Executes a HTTP Request")
  @OutputResolver(output = HttpMetadataResolver.class)
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
                      CompletionCallback<InputStream, HttpResponseAttributes> callback) {
    try {
      HttpRequesterRequestBuilder resolvedBuilder = requestBuilder != null ? requestBuilder : new HttpRequesterRequestBuilder();

      String resolvedUri;
      if (uriSettings.getUrl() == null) {
        UriParameters uriParameters = client.getDefaultUriParameters();
        String resolvedBasePath = config.getBasePath();
        String resolvedPath = resolvedBuilder.replaceUriParams(buildPath(resolvedBasePath, uriSettings.getPath()));
        resolvedUri = resolveUri(uriParameters.getScheme(), uriParameters.getHost(), uriParameters.getPort(), resolvedPath);
      } else {
        resolvedUri = resolvedBuilder.replaceUriParams(uriSettings.getUrl());
      }

      Integer resolvedTimeout = resolveResponseTimeout(overrides.getResponseTimeout());
      ResponseValidator responseValidator = responseValidationSettings.getResponseValidator();
      responseValidator = responseValidator != null ? responseValidator : new SuccessStatusCodeValidator("0..399");


      HttpRequester requester =
          new HttpRequester.Builder()
              .setConfig(config)
              .setUri(resolvedUri)
              .setMethod(method)
              .setFollowRedirects(overrides.getFollowRedirects())
              .setRequestStreamingMode(overrides.getRequestStreamingMode())
              .setSendBodyMode(overrides.getSendBodyMode())
              .setAuthentication(client.getDefaultAuthentication())
              .setResponseTimeout(resolvedTimeout)
              .setResponseValidator(responseValidator)
              .setTransformationService(muleContext.getTransformationService()).setScheduler(scheduler)
              .build();

      requester.doRequest(client, resolvedBuilder, true, muleContext, callback);
    } catch (Throwable t) {
      callback.error(t instanceof Exception ? (Exception) t : new DefaultMuleException(t));
    }
  }

  private String resolveUri(HttpConstants.Protocol scheme, String host, Integer port, String path) {
    // Encode spaces to generate a valid HTTP request.
    return scheme.getScheme() + "://" + host + ":" + port + encodeSpaces(path);
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
    this.scheduler = schedulerService.ioScheduler();
  }

  @Override
  public void dispose() {
    if (this.scheduler != null) {
      scheduler.shutdownNow();
    }
  }
}
