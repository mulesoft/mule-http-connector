/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.authentication.UsernamePasswordAuthentication;
import org.mule.extension.http.internal.HttpMetadataResolver;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.PollingSource;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@MediaType(value = ANY, strict = false)
@MetadataScope(outputResolver = HttpMetadataResolver.class)
public class HttpPollingSource extends PollingSource<Object, HttpResponseAttributes> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpPollingSource.class);

  @Connection
  private ConnectionProvider<HttpExtensionClient> clientProvider;

  @Config
  private HttpRequesterConfig config;

  @Inject
  private MuleContext muleContext;

  private HttpExtensionClient client;

  @Override
  protected void doStart() throws MuleException {
    LOGGER.error("Starting source");
    client = clientProvider.connect();
  }

  @Override
  protected void doStop() {
    LOGGER.error("Stopping source");
  }

  @Override
  public void poll(PollContext<Object, HttpResponseAttributes> pollContext) {
    HttpRequest request = HttpRequest.builder()
        .uri("https://www.google.com/teapot")
        .method(GET)
        .build();

    LOGGER.error("POLL");
    client.send(request, 9999999, false, resolveAuthentication(client.getDefaultAuthentication()))
        .whenComplete((response, exception) -> {
          pollContext.accept(item -> {
            item.setResult(new HttpResponseToResult().convert(config, muleContext, response, response.getEntity(),
                                                              response.getEntity()::getContent, request.getUri()));
          });
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
  public void onRejectedItem(Result<Object, HttpResponseAttributes> result, SourceCallbackContext sourceCallbackContext) {
    LOGGER.error("onRejectedItem");
  }
}
