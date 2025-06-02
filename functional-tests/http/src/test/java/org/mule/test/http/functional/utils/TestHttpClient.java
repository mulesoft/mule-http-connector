/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.utils;

import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import static java.lang.Integer.MAX_VALUE;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.sdk.api.http.HttpService;
import org.mule.sdk.api.http.client.HttpClient;
import org.mule.sdk.api.http.client.HttpRequestOptionsConfigurer;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.http.domain.message.response.HttpResponse;
import org.mule.sdk.api.http.sse.client.SseSource;
import org.mule.sdk.api.http.sse.client.SseSourceConfigurer;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.rules.ExternalResource;

/**
 * Defines a {@link HttpClient} using a default implementation of {@link HttpService}
 *
 * <p/>
 * This rule is intended to simplify the usage of the {@link HttpClient} as it will be started/stopped as part of the test
 * lifecycle.
 */
public class TestHttpClient extends ExternalResource implements HttpClient {

  private final HttpService httpService;
  private TlsContextFactory tlsContextFactory;
  private HttpClient httpClient;

  private TestHttpClient(HttpService httpService) {
    checkArgument(httpService != null, "httpService cannot be null");
    this.httpService = httpService;

    // TODO: move
    httpClient = httpService.client(configurer -> {
      configurer.setName(getClass().getSimpleName());
      if (tlsContextFactory != null) {
        configurer.setTlsContextFactory(tlsContextFactory);
      }
    });
    httpClient.start();
  }

  @Override
  protected void before() throws Throwable {}

  @Override
  protected void after() {
    if (httpClient != null) {
      httpClient.stop();
    }
  }

  @Override
  public CompletableFuture<HttpResponse> sendAsync(HttpRequest request,
                                                   Consumer<HttpRequestOptionsConfigurer> configurerConsumer) {
    return httpClient.sendAsync(request, configurer -> {
      configurerConsumer.accept(configurer);
      configurer.setResponseTimeout(MAX_VALUE);
    });
  }

  @Override
  public SseSource sseSource(Consumer<SseSourceConfigurer> configConsumer) {
    return httpClient.sseSource(configConsumer);
  }

  @Override
  public void start() {
    httpClient.start();
  }

  @Override
  public void stop() {
    httpClient.stop();
  }



  public static class Builder {

    private final HttpService service;
    private TlsContextFactory tlsContextFactory;

    /**
     * Creates a builder using a custom {@link HttpService}
     *
     * @param httpService httpService instance that will be used on the client. Non null
     */
    public Builder(HttpService httpService) {
      this.service = httpService;
    }

    /**
     * @param tlsContextFactory the TLS context factory for creating the context to secure the connection
     * @return same builder instance
     */
    public Builder tlsContextFactory(TlsContextFactory tlsContextFactory) {
      this.tlsContextFactory = tlsContextFactory;

      return this;
    }

    /**
     * @param tlsContextFactorySupplier a supplier for the TLS context factory for creating the context to secure the connection
     * @return same builder instance
     */
    public Builder tlsContextFactory(Supplier<TlsContextFactory> tlsContextFactorySupplier) {
      final TlsContextFactory tlsContextFactoryLocal = tlsContextFactorySupplier.get();
      try {
        initialiseIfNeeded(tlsContextFactoryLocal);
      } catch (Exception e) {
        throw new MuleRuntimeException(e);
      }

      this.tlsContextFactory = tlsContextFactoryLocal;

      return this;
    }

    /**
     * Builds the client
     *
     * @return a non null {@link TestHttpClient} with the provided configuration
     */
    public TestHttpClient build() {
      TestHttpClient httpClient = new TestHttpClient(service);
      httpClient.tlsContextFactory = tlsContextFactory;
      return httpClient;
    }
  }

  /**
   * Parses arguments passed to the runtime environment for debug flags
   * <p>
   * Options specified in:
   * <ul>
   * <li><a href="http://docs.oracle.com/javase/6/docs/technotes/guides/jpda/conninv.html#Invocation" >javase-6</a></li>
   * <li><a href="http://docs.oracle.com/javase/7/docs/technotes/guides/jpda/conninv.html#Invocation" >javase-7</a></li>
   * <li><a href="http://docs.oracle.com/javase/8/docs/technotes/guides/jpda/conninv.html#Invocation" >javase-8</a></li>
   *
   *
   * @return true if the current JVM was started in debug mode, false otherwise.
   */
  private static boolean isDebugging() {
    for (final String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
      if ("-Xdebug".equals(argument)) {
        return true;
      } else if (argument.startsWith("-agentlib:jdwp")) {
        return true;
      }
    }
    return false;
  }
}
