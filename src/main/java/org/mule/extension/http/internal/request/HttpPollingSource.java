/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.nio.charset.Charset.defaultCharset;
import static org.mule.extension.http.internal.HttpConnectorConstants.REQUEST;
import static org.mule.extension.http.internal.request.HttpPollingSourceUtils.getItemId;
import static org.mule.extension.http.internal.request.HttpPollingSourceUtils.getItems;
import static org.mule.extension.http.internal.request.HttpPollingSourceUtils.getItemWatermark;
import static org.mule.extension.http.internal.request.HttpRequestUtils.createHttpRequester;
import static org.mule.extension.http.internal.request.UriUtils.buildPath;
import static org.mule.extension.http.internal.request.UriUtils.resolveUri;
import static org.mule.extension.http.internal.request.UriUtils.replaceUriParams;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.api.metadata.MediaType.TEXT;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.DROP;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.FAIL;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.WAIT;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.builder.HttpRequesterSimpleRequestBuilder;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.api.request.response.HttpPollingSourceExpressions;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.HttpMetadataResolver;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.el.ExpressionLanguage;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Streaming;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.source.BackPressure;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.PollingSource;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Alias("pollingSource")
@org.mule.runtime.extension.api.annotation.param.MediaType(value = org.mule.runtime.extension.api.annotation.param.MediaType.ANY,
    strict = false)
@MetadataScope(outputResolver = HttpMetadataResolver.class)
@Streaming
@BackPressure(defaultMode = WAIT, supportedModes = {DROP, WAIT, FAIL})
public class HttpPollingSource extends PollingSource<String, HttpResponseAttributes> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpPollingSource.class);
  public static final String PAYLOAD_PLACEGHOLDER = "payload";
  public static final String ITEM_PLACEGHOLDER = "item";
  public static final String ATTRIBUTES_PLACEGHOLDER = "attributes";
  public static final String WATERMARK_PLACEGHOLDER = "watermark";

  @Connection
  private ConnectionProvider<HttpExtensionClient> clientProvider;

  @Inject
  private SchedulerService schedulerService;

  @Inject
  private TransformationService transformationService;

  @Inject
  @Named("http.request.fixedHeadersRegistry")
  private HashMap<String, List<String>> injectedHeaders;

  @Config
  private HttpRequesterConfig config;

  @Inject
  private MuleContext muleContext;

  @Inject
  private ExpressionLanguage expressionLanguage;

  private HttpExtensionClient client;
  private String resolvedUri;
  private Scheduler scheduler;
  private HttpRequester httpRequester;
  private ComponentLocation location;

  /**
   * Relative path from the path set in the HTTP Requester configuration
   */
  @Parameter
  @Placement(order = 1)
  @org.mule.runtime.extension.api.annotation.param.Optional
  private String path = "";

  @Parameter
  @Placement(order = 2)
  @Example("GET")
  @org.mule.runtime.extension.api.annotation.param.Optional(defaultValue = "GET")
  private String method;

  /**
   * Validation applied to the connectivity test response.
   */
  @Parameter
  @org.mule.runtime.extension.api.annotation.param.Optional
  @DisplayName("Response Validator")
  @Placement(order = 3)
  @Expression(NOT_SUPPORTED)
  private ResponseValidator responseValidator;

  /**
   * HTTP headers the message should include.
   */
  @ParameterGroup(name = REQUEST)
  @Placement(order = 4)
  private HttpRequesterSimpleRequestBuilder requestBuilder;

  @ParameterGroup(name = "Expressions")
  @Placement(order = 5)
  private HttpPollingSourceExpressions expressions;

  private SuccessStatusCodeValidator defaultStatusCodeValidator = new SuccessStatusCodeValidator("0..399");

  private ResponseValidator getResponseValidator() {
    return responseValidator != null ? responseValidator : defaultStatusCodeValidator;
  }

  @Override
  protected void doStart() throws MuleException {
    LOGGER.debug("Starting HTTP Polling Source in {}", location.getRootContainerName());
    scheduler = schedulerService.ioScheduler();
    client = clientProvider.connect();
    httpRequester = createHttpRequester(false, muleContext);
    resolvedUri = getResolvedUri();
    requestBuilder.setExpressionLanguage(expressionLanguage);
  }

  @Override
  protected void doStop() {
    LOGGER.debug("Stopping HTTP Polling Source in {}", location.getRootContainerName());
    if (scheduler != null) {
      scheduler.stop();
    }
  }

  private String getResolvedUri() {
    UriParameters uriParameters = client.getDefaultUriParameters();
    String resolvedPath = replaceUriParams(buildPath(config.getBasePath(), path), requestBuilder.getRequestUriParams());
    return resolveUri(uriParameters.getScheme(), uriParameters.getHost().trim(), uriParameters.getPort(), resolvedPath);
  }

  private Consumer<PollContext.PollItem<String, HttpResponseAttributes>> getPollingItemConsumer(TypedValue<String> fullResponse,
                                                                                                Result<TypedValue<?>, HttpResponseAttributes> item,
                                                                                                Serializable watermark) {
    return pollItem -> {
      LOGGER.debug("Setting Result for {}: {}", location.getRootContainerName(), item.getOutput());
      pollItem.setResult(toStringResult(item));
      expressions.getIdExpression()
          .ifPresent(idExp -> pollItem.setId(getItemId(fullResponse, idExp, watermark, item, expressionLanguage)));
      expressions.getWatermarkExpression()
          .ifPresent(wExp -> pollItem.setWatermark(getItemWatermark(fullResponse, wExp, watermark, item, expressionLanguage)));
    };
  }

  private void pollResult(PollContext<String, HttpResponseAttributes> pollContext,
                          Result<InputStream, HttpResponseAttributes> result, Serializable currentWatermark) {
    HttpResponseAttributes attributes = result.getAttributes().orElse(null);
    MediaType mediaType = result.getMediaType().orElse(ANY);
    Charset charset = mediaType.getCharset().orElse(defaultCharset());
    TypedValue<String> response = toTypedValue(IOUtils.toString(result.getOutput(), charset), mediaType, charset);
    LOGGER.debug("Received response at {}: {} and headers {}", location.getRootContainerName(), response,
                 attributes.getHeaders());

    Reference<Boolean> atLeastOneResult = new Reference<>(false);
    getItems(response, attributes, currentWatermark, expressions.getSplitExpression(), expressionLanguage).forEach(item -> {
      atLeastOneResult.set(true);
      pollContext.accept(getPollingItemConsumer(response, item, currentWatermark));
    });

    if (!atLeastOneResult.get()) {
      LOGGER.debug("Empty result in HTTP Polling Source at {} of uri {}", location.getRootContainerName(), resolvedUri);
    }
  }

  private void sendRequest(PollContext<String, HttpResponseAttributes> pollContext) throws InterruptedException {
    Serializable currentWatermark = pollContext.getWatermark().orElse(null);
    requestBuilder.updateWatermark(currentWatermark);

    LOGGER.debug("Sending '{}' request to '{}' in flow '{}'.", method, resolvedUri, location.getRootContainerName());
    try {
      Result<InputStream, HttpResponseAttributes> result =
          httpRequester.doSyncRequest(client, config, resolvedUri, method, config.getRequestStreamingMode(),
                                      config.getSendBodyMode(), config.getFollowRedirects(), client.getDefaultAuthentication(),
                                      config.getResponseTimeout(), getResponseValidator(), transformationService, requestBuilder,
                                      true, muleContext, scheduler, injectedHeaders)
              .get();
      pollResult(pollContext, result, currentWatermark);
    } catch (ExecutionException e) {
      LOGGER.error("There was an error in HTTP Polling Source at {} of uri '{}'", location.getRootContainerName(), resolvedUri,
                   e);
    }

  }

  @Override
  public void poll(PollContext<String, HttpResponseAttributes> pollContext) {
    if (pollContext.isSourceStopping()) {
      return;
    }

    try {
      sendRequest(pollContext);
    } catch (InterruptedException e) {
      // do nothing
    }
  }

  @Override
  public void onRejectedItem(Result<String, HttpResponseAttributes> result, SourceCallbackContext sourceCallbackContext) {
    LOGGER.debug("Item rejected by HTTP Polling Source in flow '{}', result: '{}'", location.getRootContainerName(),
                 result.getOutput());
  }

  private static Result<String, HttpResponseAttributes> toStringResult(Result<TypedValue<?>, HttpResponseAttributes> org) {
    return Result.<String, HttpResponseAttributes>builder().attributes(org.getAttributes().get())
        .output(org.getOutput().getValue().toString()).mediaType(org.getMediaType().get()).build();
  }

  private static TypedValue<String> toTypedValue(String value, MediaType mediaType, Charset encoding) {
    if (mediaType.equals(TEXT.withCharset(mediaType.getCharset().orElse(null)))) {
      return TypedValue.of(value);
    } else {
      return new TypedValue<>(value, DataType.builder().mediaType(mediaType).charset(encoding).build());
    }
  }
}
