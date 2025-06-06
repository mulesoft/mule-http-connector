/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.api.request.HttpSendBodyMode.ALWAYS;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.sdk.api.http.client.HttpClient;
import org.mule.sdk.api.http.client.auth.HttpAuthenticationConfig;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.http.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Wrapper implementation of an {@link HttpClient} that allows being shared by only configuring the client when first required and
 * only disabling it when last required.
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
    // In case this fails, we do not want the usageCount to be re-incremented
    // as it will not be further used.
    // It shouldn't be the case that more than two stops happen.
    if (--usageCount == 0) {
      delegate.stop();
    }
  }

  public CompletableFuture<HttpResponse> sendAsync(HttpRequest request, int responseTimeout, boolean followRedirects,
                                                   Consumer<HttpAuthenticationConfig> authenticationConfigurer,
                                                   HttpSendBodyMode sendBodyMode) {
    // TODO: Auth!
    return delegate.sendAsync(request, options -> options
        .setResponseTimeout(responseTimeout)
        .setFollowsRedirect(followRedirects)
        .setAuthentication(authenticationConfigurer)
        .setSendBodyAlways(sendBodyMode.equals(ALWAYS)));
  }
}
