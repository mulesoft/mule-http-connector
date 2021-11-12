/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.internal.request.HttpRequestUtils.createHttpRequester;
import static org.mule.extension.http.internal.request.HttpRequestUtils.handleCursor;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.authentication.UsernamePasswordAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.HttpMetadataResolver;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Streaming;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.source.BackPressure;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.source.BackPressureMode;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.PollingSource;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@Alias("pollingSource")
@MediaType(value = ANY, strict = false)
@MetadataScope(outputResolver = HttpMetadataResolver.class)
@Streaming
@BackPressure(defaultMode = BackPressureMode.FAIL, supportedModes = {BackPressureMode.FAIL})
public class HttpPollingSource extends PollingSource<InputStream, HttpResponseAttributes> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpPollingSource.class);

  @Connection
  private ConnectionProvider<HttpExtensionClient> clientProvider;

  @Inject
  private SchedulerService schedulerService;
  private Scheduler scheduler;
  @Inject
  private TransformationService transformationService;
  @Inject
  @Named("http.request.fixedHeadersRegistry")
  private HashMap<String, List<String>> injectedHeaders;


  private HttpRequesterRequestBuilder defaultRequestBuilder;
  private HttpRequester httpRequester;

  @Config
  private HttpRequesterConfig config;

  @Inject
  private MuleContext muleContext;

  private HttpExtensionClient client;

  //  private final String watermarkExpression;
  //  private final String identityExpression;
  //  private final String itemsExpression;
  //  private final String requestBodyExpression;
  //  private final String eventExpression;

  /**
   * Relative path from the path set in the HTTP Requester configuration
   */
  @Parameter
  @Placement(order = 1)
  @Optional
  private String path;

  @Parameter
  @Placement(order = 2)
  @Example("GET")
  @Optional(defaultValue = "GET")
  private String method;

  @Parameter
  @Placement(order = 3)
  @Example("0..399")
  @Optional(defaultValue =  "0..399")
  private String responseValidatorCodes;
  private SuccessStatusCodeValidator responseValidator;

  @Override
  protected void doStart() throws MuleException {
    LOGGER.error("Starting source");
    responseValidator = new SuccessStatusCodeValidator(responseValidatorCodes);
    scheduler = schedulerService.ioScheduler();
    defaultRequestBuilder = new HttpRequesterRequestBuilder();
    client = clientProvider.connect();
    httpRequester = createHttpRequester(false, muleContext);
  }

  @Override
  protected void doStop() {
    LOGGER.error("Stopping source");

  }

  private void sendRequest(PollContext<InputStream, HttpResponseAttributes> pollContext) {
    HttpRequesterRequestBuilder resolvedBuilder = defaultRequestBuilder;
    handleCursor(resolvedBuilder);
    //resolvedBuilder.setCorrelationInfo(correlationInfo);
    String resolvedUri = uriSettings.getResolvedUri(client, config.getBasePath(), resolvedBuilder);

    CompletionCallback<InputStream, HttpResponseAttributes> callback = new CompletionCallback<InputStream, HttpResponseAttributes>() {
      @Override
      public void success(Result<InputStream, HttpResponseAttributes> result) {
        pollContext.accept(item -> {
          item.setResult(result);
        });
      }

      @Override
      public void error(Throwable throwable) {
        LOGGER.error("There was an error", throwable);
      }
    };

    LOGGER.debug("Sending '{}' request to '{}'.", method, resolvedUri);
    httpRequester.doRequest(client, config, resolvedUri, method, config.getRequestStreamingMode(),
            config.getSendBodyMode(),
            config.getFollowRedirects(), client.getDefaultAuthentication(), config.getResponseTimeout(),
            responseValidator,
            transformationService, resolvedBuilder, true, muleContext, scheduler, notificationEmitter,
            streamingHelper, callback, injectedHeaders, null);
            //correlationInfo.getCorrelationId());
  }

  @Override
  public void poll(PollContext<InputStream, HttpResponseAttributes> pollContext) {
    HttpRequest request = HttpRequest.builder()
        .uri(config.getBasePath())
        .method(GET)
        .build();

    LOGGER.trace("POLL");
    sendRequest(pollContext);
    client.send(request, config.getResponseTimeout(), config.getFollowRedirects(),
                resolveAuthentication(client.getDefaultAuthentication()))
        .whenComplete((response, exception) -> {

        });
  }

  private HttpAuthentication resolveAuthentication(HttpRequestAuthentication authentication) {
    HttpAuthentication requestAuthentication = null;
    if (authentication instanceof UsernamePasswordAuthentication) {
      requestAuthentication = (HttpAuthentication) authentication;
    }
    return requestAuthentication;
  }

  @Override
  public void onRejectedItem(Result<InputStream, HttpResponseAttributes> result, SourceCallbackContext sourceCallbackContext) {
    LOGGER.error("onRejectedItem");
  }
}
