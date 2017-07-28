/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors.runner;

import org.mule.modules.cors.endpoint.Endpoint;
import org.mule.tck.junit4.rule.DynamicPort;

public class CorsHttpEndpoint implements Endpoint {

  private final String path;
  private final DynamicPort port;

  public CorsHttpEndpoint(String path, DynamicPort port) {
    this.path = path;
    this.port = port;
  }

  public String path() {
    return path;
  }

  public String port() {
    return port.getValue();
  }
}
