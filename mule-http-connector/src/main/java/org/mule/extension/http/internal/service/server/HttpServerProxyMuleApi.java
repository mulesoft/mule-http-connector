/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import org.mule.runtime.http.api.server.HttpServer;
import org.mule.runtime.http.api.server.RequestHandlerManager;

import java.io.IOException;
import java.util.List;

public class HttpServerProxyMuleApi implements HttpServerProxy {

  private final HttpServer delegate;

  public HttpServerProxyMuleApi(HttpServer delegate) {
    this.delegate = delegate;
  }

  public void start() throws IOException {

  }

  public void stop() {

  }

  public boolean isStopped() {
    return false;
  }

  public boolean isStopping() {
    return false;
  }

  public void dispose() {

  }

  public String getIp() {
    return "";
  }

  public int getPort() {
    return 0;
  }

  public RequestHandlerManager addRequestHandler(List<String> list, String path, RequestHandlerProxy requestHandler) {
    return null;
  }

  public RequestHandlerManager addRequestHandler(String path, RequestHandlerProxy requestHandler) {
    return null;
  }
}
