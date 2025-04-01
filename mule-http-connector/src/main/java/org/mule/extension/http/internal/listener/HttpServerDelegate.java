/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import org.mule.extension.http.internal.service.server.HttpServerProxy;
import org.mule.extension.http.internal.service.server.RequestHandlerProxy;
import org.mule.runtime.http.api.server.HttpServer;
import org.mule.runtime.http.api.server.RequestHandlerManager;

import java.io.IOException;
import java.util.List;

/**
 * Base class for applying the delegate design pattern around an {@link HttpServer}
 */
public class HttpServerDelegate implements HttpServerProxy {

  private final HttpServerProxy delegate;

  HttpServerDelegate(HttpServerProxy delegate) {
    this.delegate = delegate;
  }

  @Override
  public void start() throws IOException {
    delegate.start();
  }

  @Override
  public boolean isStopped() {
    return delegate.isStopped();
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
  public boolean isStopping() {
    return false;
  }

  @Override
  public String getIp() {
    return "";
  }

  @Override
  public int getPort() {
    return 0;
  }

  @Override
  public RequestHandlerManager addRequestHandler(List<String> list, String path, RequestHandlerProxy requestHandler) {
    return null;
  }

  @Override
  public RequestHandlerManager addRequestHandler(String path, RequestHandlerProxy requestHandler) {
    return null;
  }
}
