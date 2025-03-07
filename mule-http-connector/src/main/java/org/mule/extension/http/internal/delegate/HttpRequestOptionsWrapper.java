/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.delegate;

import org.mule.extension.http.api.request.proxy.HttpProxyConfig;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.sdk.api.http.HttpRequestOptions;

import java.util.Optional;

public class HttpRequestOptionsWrapper implements HttpRequestOptions<HttpAuthentication, HttpProxyConfig> {

  private final int responseTimeout;
  private final boolean followsRedirect;
  private final HttpAuthentication authentication;
  private final HttpProxyConfig proxyConfig;
  private final boolean sendBodyAlways;

  public HttpRequestOptionsWrapper(int responseTimeout, boolean followsRedirect, HttpAuthentication authentication,
                                   HttpProxyConfig proxyConfig, boolean sendBodyAlways) {
    this.responseTimeout = responseTimeout;
    this.followsRedirect = followsRedirect;
    this.authentication = authentication;
    this.proxyConfig = proxyConfig;
    this.sendBodyAlways = sendBodyAlways;
  }

  @Override
  public int getResponseTimeout() {
    return responseTimeout;
  }

  @Override
  public boolean isFollowsRedirect() {
    return followsRedirect;
  }

  @Override
  public Optional<HttpAuthentication> getAuthentication() {
    return Optional.ofNullable(authentication);
  }

  @Override
  public boolean shouldSendBodyAlways() {
    return sendBodyAlways;
  }

  @Override
  public Optional<HttpProxyConfig> getProxyConfig() {
    return Optional.ofNullable(proxyConfig);
  }
}
