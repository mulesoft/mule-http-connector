/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import org.mule.sdk.api.http.HttpConstants;
import org.mule.sdk.api.http.server.EndpointAvailabilityHandler;
import org.mule.sdk.api.http.server.HttpServer;
import org.mule.sdk.api.http.server.RequestHandler;
import org.mule.sdk.api.http.server.ServerAddress;
import org.mule.sdk.api.http.sse.server.SseClient;
import org.mule.sdk.api.http.sse.server.SseEndpointManager;
import org.mule.sdk.api.http.sse.server.SseRequestContext;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Base class for applying the delegate design pattern around an {@link HttpServer}
 */
public class HttpServerDelegate implements HttpServer {

  private final HttpServer delegate;

  HttpServerDelegate(HttpServer delegate) {
    this.delegate = delegate;
  }

  @Override
  public void start() throws IOException {
    delegate.start();
  }

  @Override
  public void stop() {
    delegate.stop();
  }

  @Override
  public void dispose() {
    delegate.dispose();
  }

  @Override
  public ServerAddress getServerAddress() {
    return delegate.getServerAddress();
  }

  @Override
  public HttpConstants.Protocol getProtocol() {
    return delegate.getProtocol();
  }

  @Override
  public boolean isStopping() {
    return delegate.isStopping();
  }

  @Override
  public boolean isStopped() {
    return delegate.isStopped();
  }

  @Override
  public EndpointAvailabilityHandler addRequestHandler(Collection<String> methods, String path, RequestHandler requestHandler) {
    return delegate.addRequestHandler(methods, path, requestHandler);
  }

  @Override
  public EndpointAvailabilityHandler addRequestHandler(String path, RequestHandler requestHandler) {
    return delegate.addRequestHandler(path, requestHandler);
  }

  @Override
  public SseEndpointManager sse(String ssePath, Consumer<SseRequestContext> onRequest, Consumer<SseClient> onClient) {
    // TODO: implement...
    return null;
  }

  protected HttpServer getDelegate() {
    return delegate;
  }
}
