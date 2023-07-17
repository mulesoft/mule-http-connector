/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Wrapper implementation of an {@link HttpClient} that allows being shared by only configuring the client when first required
 * and only disabling it when last required.
 */
public class ShareableHttpClient {

  private HttpClient delegate;
  private Integer usageCount = new Integer(0);

  public ShareableHttpClient(HttpClient client) {
    delegate = client;
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
    return HttpClientReflection.sendAsync(delegate, request, responseTimeout, followRedirects, authentication, sendBodyMode);
  }
}
