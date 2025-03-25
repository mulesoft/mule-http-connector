/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.delegate;

import org.mule.extension.http.api.request.proxy.HttpProxyConfig;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.sdk.api.http.HttpClient;
import org.mule.sdk.api.http.HttpRequestOptions;
import org.mule.sdk.api.http.sse.ServerSentEventSource;
import org.mule.sdk.api.http.sse.SseRetryConfig;

import java.util.concurrent.CompletableFuture;

public class HttpClientWrapper
    implements HttpClient<HttpRequest, HttpRequestOptions<HttpAuthentication, HttpProxyConfig>, HttpResponse> {

  private final org.mule.runtime.http.api.client.HttpClient delegate;

  public HttpClientWrapper(org.mule.runtime.http.api.client.HttpClient delegate) {
    this.delegate = delegate;
  }

  @Override
  public CompletableFuture<HttpResponse> sendAsync(HttpRequest request,
                                                   HttpRequestOptions<HttpAuthentication, HttpProxyConfig> o) {
    // TODO: use reflection client instead
    return delegate.sendAsync(request, o.getResponseTimeout(), o.isFollowsRedirect(), o.getAuthentication().orElse(null));
  }

  @Override
  public ServerSentEventSource sseSource(String url, SseRetryConfig retryConfig) {
    throw new UnsupportedOperationException("Server-sent events are not supported");
  }

  @Override
  public void start() {
    delegate.start();
  }

  @Override
  public void stop() {
    delegate.stop();
  }
}
