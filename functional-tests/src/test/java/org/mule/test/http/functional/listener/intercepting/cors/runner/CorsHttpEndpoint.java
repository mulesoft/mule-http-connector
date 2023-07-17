/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors.runner;

import org.mule.modules.cors.endpoint.KernelTestEndpoint;

public class CorsHttpEndpoint implements KernelTestEndpoint {

  private final String path;
  private final String port;

  public CorsHttpEndpoint(String path, String port) {
    this.path = path;
    this.port = port;
  }

  public String path() {
    return path;
  }

  public String port() {
    return port;
  }
}
