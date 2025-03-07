/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.delegate;

import org.mule.extension.http.api.request.proxy.HttpProxyConfig;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.http.api.server.HttpServerFactory;
import org.mule.sdk.api.http.HttpClientFactory;
import org.mule.sdk.api.http.HttpRequestOptions;
import org.mule.sdk.api.http.HttpRequestOptionsBuilder;
import org.mule.sdk.api.http.HttpServiceApi;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

public class HttpServiceApiProxy implements
    HttpServiceApi<HttpClientFactory<HttpClientConfiguration, HttpRequest, HttpRequestOptions<HttpAuthentication, HttpProxyConfig>, HttpResponse>, HttpServerFactory, HttpAuthentication, HttpProxyConfig> {

  @Inject
  private HttpService legacyApi;

  @Inject
  @Named("_httpServiceApi")
  private Optional<HttpServiceApi> forwardCompatibilityApi;

  @Override
  public HttpClientFactory<HttpClientConfiguration, HttpRequest, HttpRequestOptions<HttpAuthentication, HttpProxyConfig>, HttpResponse> getClientFactory() {
    if (forwardCompatibilityApi.isPresent()) {
      return ((HttpServiceApi<HttpClientFactory<HttpClientConfiguration, HttpRequest, HttpRequestOptions<HttpAuthentication, HttpProxyConfig>, HttpResponse>, HttpServerFactory, HttpAuthentication, HttpProxyConfig>) forwardCompatibilityApi
          .get()).getClientFactory();
    } else {
      return new HttpClientFactoryWrapper(legacyApi.getClientFactory());
    }
  }

  @Override
  public HttpServerFactory getServerFactory() {
    if (forwardCompatibilityApi.isPresent()) {
      return ((HttpServiceApi<HttpClientFactory<HttpClientConfiguration, HttpRequest, HttpRequestOptions<HttpAuthentication, HttpProxyConfig>, HttpResponse>, HttpServerFactory, HttpAuthentication, HttpProxyConfig>) forwardCompatibilityApi
          .get()).getServerFactory();
    } else {
      return legacyApi.getServerFactory();
    }
  }

  @Override
  public HttpRequestOptionsBuilder<HttpAuthentication, HttpProxyConfig> requestOptionsBuilder() {
    if (forwardCompatibilityApi.isPresent()) {
      return ((HttpServiceApi<HttpClientFactory<HttpClientConfiguration, HttpRequest, HttpRequestOptions<HttpAuthentication, HttpProxyConfig>, HttpResponse>, HttpServerFactory, HttpAuthentication, HttpProxyConfig>) forwardCompatibilityApi
          .get()).requestOptionsBuilder();
    } else {
      return new HttpRequestOptionsBuilderWrapper();
    }
  }
}
