/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import org.mule.runtime.http.api.server.RequestHandlerManager;

import java.io.IOException;
import java.util.List;

public interface HttpServerProxy {

  void start() throws IOException;

  void stop();

  boolean isStopped();

  boolean isStopping();

  void dispose();

  String getIp();

  int getPort();

  RequestHandlerManager addRequestHandler(List<String> list, String path, RequestHandlerProxy requestHandler);

  RequestHandlerManager addRequestHandler(String path, RequestHandlerProxy requestHandler);
}
