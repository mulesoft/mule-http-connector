/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service;

import org.mule.sdk.api.http.HttpService;
import org.mule.sdk.api.http.client.ClientCreationException;
import org.mule.sdk.api.http.client.HttpClient;
import org.mule.sdk.api.http.client.HttpClientConfig;
import org.mule.sdk.api.http.domain.entity.HttpEntityFactory;
import org.mule.sdk.api.http.domain.message.request.HttpRequestBuilder;
import org.mule.sdk.api.http.domain.message.response.HttpResponse;
import org.mule.sdk.api.http.domain.message.response.HttpResponseBuilder;
import org.mule.sdk.api.http.server.HttpServer;
import org.mule.sdk.api.http.server.HttpServerConfig;
import org.mule.sdk.api.http.server.ServerCreationException;

import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

public class HttpServiceDelegate implements HttpService {

  private HttpService delegate;

  @Inject
  public HttpServiceDelegate(Optional<HttpService> sdkApiServiceOptional,
                             org.mule.runtime.http.api.HttpService muleHttpApiServiceFallback) {
    this.delegate = sdkApiServiceOptional.orElse(new MuleApiImplementationWrapper(muleHttpApiServiceFallback));
  }

  public HttpServiceDelegate() {}

  @Override
  public HttpClient client(Consumer<HttpClientConfig> configCallback) throws ClientCreationException {
    return delegate.client(configCallback);
  }

  @Override
  public HttpServer server(Consumer<HttpServerConfig> configCallback) throws ServerCreationException {
    return delegate.server(configCallback);
  }

  @Override
  public HttpResponseBuilder responseBuilder() {
    return delegate.responseBuilder();
  }

  @Override
  public HttpResponseBuilder responseBuilder(HttpResponse original) {
    return delegate.responseBuilder(original);
  }

  @Override
  public HttpRequestBuilder requestBuilder() {
    return delegate.requestBuilder();
  }

  @Override
  public HttpRequestBuilder requestBuilder(boolean preserveHeaderCase) {
    return delegate.requestBuilder(preserveHeaderCase);
  }

  @Override
  public HttpEntityFactory entityFactory() {
    return delegate.entityFactory();
  }
}
