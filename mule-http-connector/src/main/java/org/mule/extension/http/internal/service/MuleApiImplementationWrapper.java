/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service;

import org.mule.extension.http.internal.service.client.HttpClientConfigToBuilder;
import org.mule.extension.http.internal.service.client.HttpClientWrapper;
import org.mule.extension.http.internal.service.message.HttpEntityFactoryImpl;
import org.mule.extension.http.internal.service.message.HttpRequestBuilderWrapper;
import org.mule.extension.http.internal.service.message.HttpResponseBuilderWrapper;
import org.mule.extension.http.internal.service.server.HttpServerConfigurerToBuilder;
import org.mule.extension.http.internal.service.server.HttpServerWrapper;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.server.HttpServerConfiguration;
import org.mule.sdk.api.http.HttpService;
import org.mule.sdk.api.http.client.HttpClient;
import org.mule.sdk.api.http.client.HttpClientConfig;
import org.mule.sdk.api.http.domain.entity.HttpEntityFactory;
import org.mule.sdk.api.http.domain.message.request.HttpRequestBuilder;
import org.mule.sdk.api.http.domain.message.response.HttpResponse;
import org.mule.sdk.api.http.domain.message.response.HttpResponseBuilder;
import org.mule.sdk.api.http.server.HttpServer;
import org.mule.sdk.api.http.server.HttpServerConfigurer;

import java.util.function.Consumer;

public class MuleApiImplementationWrapper implements HttpService {

  private final org.mule.runtime.http.api.HttpService httpService;

  public MuleApiImplementationWrapper(org.mule.runtime.http.api.HttpService httpService) {
    this.httpService = httpService;
  }

  @Override
  public HttpClient client(Consumer<HttpClientConfig> configBuilder) {
    HttpClientConfiguration.Builder builder = new HttpClientConfiguration.Builder();
    HttpClientConfigToBuilder configurer = new HttpClientConfigToBuilder(builder);
    configBuilder.accept(configurer);
    HttpClientConfiguration configuration = builder.build();
    try {
      return new HttpClientWrapper(httpService.getClientFactory().create(configuration));
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public HttpServer server(Consumer<HttpServerConfigurer> configBuilder)
      throws org.mule.sdk.api.http.server.ServerCreationException {
    HttpServerConfiguration.Builder builder = new HttpServerConfiguration.Builder();
    HttpServerConfigurerToBuilder configurer = new HttpServerConfigurerToBuilder(builder);
    configBuilder.accept(configurer);
    HttpServerConfiguration configuration = builder.build();
    try {
      return new HttpServerWrapper(httpService.getServerFactory().create(configuration));
    } catch (org.mule.runtime.http.api.server.ServerCreationException e) {
      throw new org.mule.sdk.api.http.server.ServerCreationException(e.getMessage());
    }
  }

  @Override
  public HttpResponseBuilder responseBuilder() {
    return new HttpResponseBuilderWrapper(org.mule.runtime.http.api.domain.message.response.HttpResponse.builder());
  }

  @Override
  public HttpResponseBuilder responseBuilder(HttpResponse original) {
    return responseBuilder().statusCode(original.getStatusCode()).reasonPhrase(original.getReasonPhrase());
  }

  @Override
  public HttpRequestBuilder requestBuilder() {
    return new HttpRequestBuilderWrapper(org.mule.runtime.http.api.domain.message.request.HttpRequest.builder());
  }

  @Override
  public HttpRequestBuilder requestBuilder(boolean preserveHeaderCase) {
    return new HttpRequestBuilderWrapper(org.mule.runtime.http.api.domain.message.request.HttpRequest
        .builder(preserveHeaderCase));
  }

  @Override
  public HttpEntityFactory entityFactory() {
    return new HttpEntityFactoryImpl();
  }
}
