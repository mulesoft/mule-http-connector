/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Optional.empty;
import static org.mule.extension.http.internal.HttpConnectorConstants.REQUEST;
import static org.mule.extension.http.internal.request.HttpRequestUtils.createHttpRequester;
import static org.mule.extension.http.internal.request.UriUtils.buildPath;
import static org.mule.extension.http.internal.request.UriUtils.resolveUri;
import static org.mule.extension.http.internal.request.UriUtils.replaceUriParams;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA;
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
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.el.ExpressionLanguage;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
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
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.source.BackPressure;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.PollingSource;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Alias("pollingSource")
@org.mule.runtime.extension.api.annotation.param.MediaType(value = org.mule.runtime.extension.api.annotation.param.MediaType.ANY,
    strict = false)
@MetadataScope(outputResolver = HttpMetadataResolver.class)
@Streaming
@BackPressure(defaultMode = WAIT, supportedModes = {DROP, WAIT, FAIL})
public class HttpPollingSource extends PollingSource<String, HttpResponseAttributes> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpPollingSource.class);
  private static final String PAYLOAD_PLACEGHOLDER = "payload";
  private static final String ITEM_PLACEGHOLDER = "item";
  private static final String ATTRIBUTES_PLACEGHOLDER = "attributes";

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
  @Optional
  private String path = "";

  @Parameter
  @Placement(order = 2)
  @Example("GET")
  @Optional(defaultValue = "GET")
  private String method;

  /**
   * HTTP headers the message should include.
   */
  @ParameterGroup(name = REQUEST)
  @Placement(order = 3)
  private HttpRequesterSimpleRequestBuilder requestBuilder;

  @ParameterGroup(name = "Expressions")
  @Placement(order = 4)
  private HttpPollingSourceExpressions expressions;

  /**
   * Validation applied to the connectivity test response.
   */
  @Parameter
  @Optional
  @DisplayName("Response Validator")
  @Placement(order = 7)
  @Expression(NOT_SUPPORTED)
  private ResponseValidator responseValidator;

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
  }

  @Override
  protected void doStop() {
    LOGGER.debug("Stopping HTTP Polling Source in {}", location.getRootContainerName());
    scheduler.stop();
  }

  private String getResolvedUri() {
    UriParameters uriParameters = client.getDefaultUriParameters();
    String resolvedPath = replaceUriParams(buildPath(config.getBasePath(), path), requestBuilder.getRequestUriParams());
    return resolveUri(uriParameters.getScheme(), uriParameters.getHost().trim(), uriParameters.getPort(), resolvedPath);
  }

  protected String getId() {
    return getClass().getSimpleName();
  }

  private Consumer<PollContext.PollItem<String, HttpResponseAttributes>> getPollingItemConsumer(Result<String, HttpResponseAttributes> item) {
    return pollItem -> {
      LOGGER.debug("Setting Result for {}: {}", location.getRootContainerName(), item.getOutput());
      pollItem.setResult(item);
      // TODO (HTTPC-178 and HTTPC-179 - idempotency and watermarking)
    };
  }

  private void sendRequest(PollContext<String, HttpResponseAttributes> pollContext) {
    CompletionCallback<InputStream, HttpResponseAttributes> callback =
        new CompletionCallback<InputStream, HttpResponseAttributes>() {

          @Override
          public void success(Result<InputStream, HttpResponseAttributes> result) {
            HttpResponseAttributes attributes = result.getAttributes().orElse(null);
            MediaType mediaType = result.getMediaType().orElse(ANY);
            Reference<Boolean> atLeastOneResult = new Reference<>(false);
            getItems(result.getOutput(), mediaType, attributes).forEach(item -> {
              atLeastOneResult.set(true);
              pollContext.accept(getPollingItemConsumer(item));
            });

            if (!atLeastOneResult.get()) {
              LOGGER.debug("Empty result in HTTP Polling Source at {} of uri {}", location.getRootContainerName(), resolvedUri);
            }
          }

          @Override
          public void error(Throwable throwable) {
            LOGGER.error("There was an error in HTTP Polling Source at {} of uri '{}'", location.getRootContainerName(),
                         resolvedUri, throwable);
          }
        };

    LOGGER.debug("Sending '{}' request to '{}' in flow '{}'.", method, resolvedUri, location.getRootContainerName());
    try {
      httpRequester.doRequest(client, config, resolvedUri, method, config.getRequestStreamingMode(), config.getSendBodyMode(),
                              config.getFollowRedirects(), client.getDefaultAuthentication(), config.getResponseTimeout(),
                              getResponseValidator(), transformationService, requestBuilder, true, muleContext, scheduler, null,
                              null, callback, injectedHeaders, null);
    } catch (MuleRuntimeException e) {
      LOGGER.error("Trigger '{}': Mule runtime exception found while executing poll to {}: '{}'", getId(), resolvedUri,
                   e.getMessage(), e);
    }

  }

  @Override
  public void poll(PollContext<String, HttpResponseAttributes> pollContext) {
    if (pollContext.isSourceStopping()) {
      return;
    }

    sendRequest(pollContext);
  }

  @Override
  public void onRejectedItem(Result<String, HttpResponseAttributes> result,
                             SourceCallbackContext sourceCallbackContext) {
    LOGGER.debug("Item rejected by HTTP Polling Source in flow '{}', result: '{}'", location.getRootContainerName(),
                 result.getOutput());
  }

  private BindingContext buildContext(TypedValue<String> payload, HttpResponseAttributes attributes,
                                      java.util.Optional<TypedValue<CursorStreamProvider>> item) {
    BindingContext.Builder builder = BindingContext.builder().addBinding(PAYLOAD_PLACEGHOLDER, payload);
    builder.addBinding(ATTRIBUTES_PLACEGHOLDER, TypedValue.of(attributes));
    item.ifPresent(it -> builder.addBinding(ITEM_PLACEGHOLDER, it));
    return builder.build();
  }

  private static TypedValue<String> toTypedValue(String value, MediaType mediaType, Charset encoding) {
    if (mediaType.equals(TEXT.withCharset(mediaType.getCharset().orElse(null)))) {
      return TypedValue.of(value);
    } else {
      return new TypedValue<>(value, DataType.builder().mediaType(mediaType).charset(encoding).build());
    }
  }

  private Result<String, HttpResponseAttributes> toResult(String item, MediaType mediaType, HttpResponseAttributes attributes) {
    return Result.<String, HttpResponseAttributes>builder().attributes(attributes).output(item).mediaType(mediaType).build();
  }

  private static boolean isJavaPayload(MediaType mediaType) {
    return mediaType.equals(APPLICATION_JAVA.withCharset(mediaType.getCharset().orElse(null)));
  }

  private Stream<Result<String, HttpResponseAttributes>> getItems(InputStream fullResponse, MediaType mediaType,
                                                                  HttpResponseAttributes attributes) {
    if (isJavaPayload(mediaType)) {
      throw new MuleRuntimeException(createStaticMessage(format("%s is not an accepted media type",
                                                                APPLICATION_JAVA.toRfcString())));
    }
    java.util.Optional<String> itemsExpression = expressions.getSplitExpression();
    Charset charset = mediaType.getCharset().orElse(defaultCharset());
    String response = IOUtils.toString(fullResponse, charset);
    LOGGER.debug("Received response at {}: {} and headers {}", location.getRootContainerName(), response,
                 attributes.getHeaders());
    if (!itemsExpression.isPresent()) {
      return Stream.of(toResult(response, mediaType, attributes));
    }

    TypedValue<String> typedValue = toTypedValue(response, mediaType, charset);
    Iterable<TypedValue<?>> splitted =
        () -> expressionLanguage.split(itemsExpression.get(), buildContext(typedValue, attributes, empty()));
    return StreamSupport.stream(splitted.spliterator(), false)
        .map(item -> toResult(item.getValue().toString(), item.getDataType().getMediaType(), attributes));
  }

}
