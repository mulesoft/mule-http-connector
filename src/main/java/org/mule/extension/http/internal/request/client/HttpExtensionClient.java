/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.client;

import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.internal.request.HttpRequesterConnectionManager.ShareableHttpClient;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Composition of a {@link ShareableHttpClient} with URI and authentication parameters that allow falling back to connection
 * default values for them.
 *
 * @since 1.0
 */
public class HttpExtensionClient implements Startable, Stoppable {

  private final HttpRequestAuthentication authentication;
  private final ShareableHttpClient httpClient;
  private final UriParameters uriParameters;

  public HttpExtensionClient(ShareableHttpClient httpClient, UriParameters uriParameters,
                             HttpRequestAuthentication authentication) {
    this.httpClient = httpClient;
    this.uriParameters = uriParameters;
    this.authentication = authentication;
  }

  /**
   * Returns the default parameters for the {@link HttpRequest} URI.
   */
  public UriParameters getDefaultUriParameters() {
    return uriParameters;
  }

  public HttpRequestAuthentication getDefaultAuthentication() {
    return authentication;
  }

  @Override
  public void start() throws MuleException {
    httpClient.start();
    try {
      startIfNeeded(authentication);
    } catch (Exception e) {
      httpClient.stop();
      throw e;
    }
  }

  @Override
  public void stop() throws MuleException {
    stopIfNeeded(authentication);
    httpClient.stop();
  }

  public CompletableFuture<HttpResponse> send(HttpRequest request, int responseTimeout, boolean followRedirects,
                                              HttpAuthentication authentication,
                                              HttpSendBodyMode sendBodyMode) {
    return httpClient.sendAsync(request, responseTimeout, followRedirects, authentication, sendBodyMode);
  }
}
