/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.internal.HttpConnectorConstants.REQUEST;
import static org.mule.extension.http.internal.request.HttpRequestUtils.createHttpRequester;
import static org.mule.extension.http.internal.request.UriUtils.buildPath;
import static org.mule.extension.http.internal.request.UriUtils.resolveUri;
import static org.mule.extension.http.internal.request.UriUtils.replaceUriParams;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.DROP;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.FAIL;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.WAIT;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.builder.HttpRequesterSimpleRequestBuilder;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.HttpMetadataResolver;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Streaming;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
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
import java.util.HashMap;
import java.util.List;

@Alias("pollingSource")
@MediaType(value = ANY, strict = false)
@MetadataScope(outputResolver = HttpMetadataResolver.class)
@Streaming
@BackPressure(defaultMode = WAIT, supportedModes = {DROP, WAIT, FAIL})
public class HttpPollingSource extends PollingSource<String, HttpResponseAttributes> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpPollingSource.class);

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

  // TODO (HTTPC-181) make this a parameter group
  private ResponseValidator responseValidator = new SuccessStatusCodeValidator("0..399");

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

  private void sendRequest(PollContext<String, HttpResponseAttributes> pollContext) {
    CompletionCallback<InputStream, HttpResponseAttributes> callback =
        new CompletionCallback<InputStream, HttpResponseAttributes>() {

          @Override
          public void success(Result<InputStream, HttpResponseAttributes> result) {
            pollContext.accept(item -> {
              // TODO (HTTPC-180): We put here splitting and etc... For now just consuming the stream
              Result.Builder<String, HttpResponseAttributes> responseBuilder =
                  Result.<String, HttpResponseAttributes>builder().output(IOUtils.toString(result.getOutput()));
              result.getAttributes().ifPresent(attr -> responseBuilder.attributes(attr));
              item.setResult(responseBuilder.build());
            });
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
                              responseValidator, transformationService, requestBuilder, true, muleContext, scheduler, null, null,
                              callback, injectedHeaders, null);
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
  public void onRejectedItem(Result<String, HttpResponseAttributes> result, SourceCallbackContext sourceCallbackContext) {
    LOGGER.debug("Item rejected by HTTP Polling Source in flow '{}' with result: '{}", location.getRootContainerName(),
                 result.getOutput());
  }
}
