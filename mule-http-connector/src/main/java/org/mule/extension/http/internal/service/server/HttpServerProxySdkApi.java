/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import org.mule.runtime.http.api.server.RequestHandlerManager;
import org.mule.sdk.api.http.server.HttpServer;

import java.io.IOException;
import java.util.List;

public class HttpServerProxySdkApi implements HttpServerProxy {

  private final HttpServer delegate;

  public HttpServerProxySdkApi(HttpServer delegate) {
    this.delegate = delegate;
  }

  @Override
  public void start() throws IOException {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean isStopped() {
    return false;
  }

  @Override
  public boolean isStopping() {
    return false;
  }

  @Override
  public void dispose() {

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
