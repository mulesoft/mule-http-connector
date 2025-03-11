/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.delegate;

import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.server.HttpServer;
import org.mule.sdk.api.http.HttpServiceApi;
import org.mule.sdk.api.http.sse.ServerSentEventSource;
import org.mule.sdk.api.http.sse.SseClient;
import org.mule.sdk.api.http.sse.SseEndpointManager;
import org.mule.sdk.api.http.sse.SseRetryConfig;

import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;

public class HttpServiceApiProxy implements HttpServiceApi<HttpClient, HttpServer> {

  @Inject
  @Named("_httpServiceApi")
  private Optional<HttpServiceApi<HttpClient, HttpServer>> forwardCompatibilityApi;

  @Override
  public SseEndpointManager sseEndpoint(HttpServer httpServer, String ssePath, Consumer<SseClient> sseClientHandler) {
    if (forwardCompatibilityApi.isPresent()) {
      HttpServiceApi<HttpClient, HttpServer> httpServiceApi = forwardCompatibilityApi.get();
      return httpServiceApi.sseEndpoint(httpServer, ssePath, sseClientHandler);
    } else {
      throw new IllegalStateException("Feature not implemented in this Mule Version");
    }
  }

  @Override
  public ServerSentEventSource sseSource(HttpClient httpClient, String url, SseRetryConfig retryConfig) {
    if (forwardCompatibilityApi.isPresent()) {
      HttpServiceApi<HttpClient, HttpServer> httpServiceApi = forwardCompatibilityApi.get();
      return httpServiceApi.sseSource(httpClient, url, retryConfig);
    } else {
      throw new IllegalStateException("Feature not implemented in this Mule Version");
    }
  }
}
