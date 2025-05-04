/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.delegate;

import org.mule.sdk.api.http.HttpServer;
import org.mule.sdk.api.http.sse.SseClient;
import org.mule.sdk.api.http.sse.SseHandlerManager;

import java.io.IOException;
import java.util.function.Consumer;

public class HttpServerWrapper implements HttpServer {

  private final org.mule.runtime.http.api.server.HttpServer delegate;

  public HttpServerWrapper(org.mule.runtime.http.api.server.HttpServer delegate) {
    this.delegate = delegate;
  }

  @Override
  public HttpServer start() throws IOException {
    delegate.start();
    return this;
  }

  @Override
  public boolean isStopped() {
    return delegate.isStopped();
  }

  @Override
  public HttpServer stop() {
    delegate.stop();
    return this;
  }

  @Override
  public void dispose() {
    delegate.dispose();
  }

  @Override
  public boolean isStopping() {
    return delegate.isStopping();
  }

  @Override
  public String getHost() {
    return delegate.getServerAddress().getIp();
  }

  @Override
  public int getPort() {
    return delegate.getServerAddress().getPort();
  }

  @Override
  public SseHandlerManager sse(String path, Consumer<SseClient> clientHandler) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
