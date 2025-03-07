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
import org.mule.sdk.api.http.HttpRequestOptionsBuilder;

public class HttpRequestOptionsBuilderWrapper implements HttpRequestOptionsBuilder<HttpAuthentication, HttpProxyConfig> {

  private int responseTimeout;
  private boolean followsRedirect;
  private HttpAuthentication authentication;
  private HttpProxyConfig proxyConfig;
  private boolean sendBodyAlways;

  @Override
  public HttpRequestOptionsBuilder<HttpAuthentication, HttpProxyConfig> responseTimeout(int responseTimeout) {
    this.responseTimeout = responseTimeout;
    return this;
  }

  @Override
  public HttpRequestOptionsBuilder<HttpAuthentication, HttpProxyConfig> followsRedirect(boolean followsRedirect) {
    this.followsRedirect = followsRedirect;
    return this;
  }

  @Override
  public HttpRequestOptionsBuilder<HttpAuthentication, HttpProxyConfig> authentication(HttpAuthentication authentication) {
    this.authentication = authentication;
    return this;
  }

  @Override
  public HttpRequestOptionsBuilder<HttpAuthentication, HttpProxyConfig> proxyConfig(HttpProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
    return this;
  }

  @Override
  public HttpRequestOptionsBuilder<HttpAuthentication, HttpProxyConfig> sendBodyAlways(boolean sendBodyAlways) {
    this.sendBodyAlways = sendBodyAlways;
    return this;
  }

  @Override
  public HttpRequestOptions<HttpAuthentication, HttpProxyConfig> build() {
    return new HttpRequestOptionsWrapper(responseTimeout, followsRedirect, authentication, proxyConfig, sendBodyAlways);
  }
}
