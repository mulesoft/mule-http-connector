/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.server.HttpServer;
import org.mule.runtime.http.api.server.RequestHandler;
import org.mule.runtime.http.api.server.RequestHandlerManager;
import org.mule.runtime.http.api.server.ServerAddress;

import java.io.IOException;
import java.util.Collection;

/**
 * Base class for applying the delegate design pattern around an {@link HttpServer}
 */
public class HttpServerDelegate implements HttpServer {

  private final HttpServer delegate;

  HttpServerDelegate(HttpServer delegate) {
    this.delegate = delegate;
  }

  @Override
  public HttpServer start() throws IOException {
    delegate.start();
    return this;
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
  public RequestHandlerManager addRequestHandler(Collection<String> methods, String path, RequestHandler requestHandler) {
    return delegate.addRequestHandler(methods, path, requestHandler);
  }

  @Override
  public RequestHandlerManager addRequestHandler(String path, RequestHandler requestHandler) {
    return delegate.addRequestHandler(path, requestHandler);
  }

  protected HttpServer getDelegate() {
    return delegate;
  }

  // Keep these methods to compile against 4.4.0 version of mule-service-http-api.

  public void enableTls(TlsContextFactory tlsContextFactory) {}

  public void disableTls() {}
}
