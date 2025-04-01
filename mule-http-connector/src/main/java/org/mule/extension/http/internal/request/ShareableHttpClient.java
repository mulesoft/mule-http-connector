/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.api.request.HttpSendBodyMode.ALWAYS;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.proxy.HttpProxyConfig;
import org.mule.extension.http.internal.delegate.HttpServiceApiProxy;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.sdk.api.http.HttpClient;
import org.mule.sdk.api.http.HttpRequestOptions;
import org.mule.sdk.api.http.HttpRequestOptionsBuilder;
import org.mule.sdk.api.http.sse.ServerSentEventSource;
import org.mule.sdk.api.http.sse.SseRetryConfig;

import java.util.concurrent.CompletableFuture;

/**
 * Wrapper implementation of an {@link HttpClient} that allows being shared by only configuring the client when first required and
 * only disabling it when last required.
 */
public class ShareableHttpClient {

  private final HttpServiceApiProxy httpService;
  private final HttpClient<HttpRequest, HttpRequestOptions<HttpAuthentication, HttpProxyConfig>, HttpResponse> delegate;
  private Integer usageCount = 0;

  public ShareableHttpClient(HttpClient<HttpRequest, HttpRequestOptions<HttpAuthentication, HttpProxyConfig>, HttpResponse> client,
                             HttpServiceApiProxy httpService) {
    delegate = client;
    this.httpService = httpService;
  }

  public synchronized void start() {
    if (++usageCount == 1) {
      try {
        delegate.start();
      } catch (Exception e) {
        usageCount--;
        throw e;
      }
    }
  }

  public synchronized void stop() {
    // In case this fails we do not want the usageCount to be reincremented
    // as it will not be further used. If shouldn't be the case that more than
    // two stops happen.
    if (--usageCount == 0) {
      delegate.stop();
    }
  }

  public CompletableFuture<HttpResponse> sendAsync(HttpRequest request, int responseTimeout, boolean followRedirects,
                                                   HttpAuthentication authentication,
                                                   HttpSendBodyMode sendBodyMode) {
    HttpRequestOptionsBuilder<HttpAuthentication, HttpProxyConfig> optionsBuilder = httpService.requestOptionsBuilder();
    return delegate.sendAsync(request, optionsBuilder
        .responseTimeout(responseTimeout)
        .followsRedirect(followRedirects)
        .authentication(authentication)
        .sendBodyAlways(ALWAYS == sendBodyMode)
        .build());
  }

  public ServerSentEventSource sseSource(String uri, SseRetryConfig retryConfig) {
    return delegate.sseSource(uri, retryConfig);
  }
}
