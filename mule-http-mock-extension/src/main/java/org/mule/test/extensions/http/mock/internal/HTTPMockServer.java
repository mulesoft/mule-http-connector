/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal;


import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class HTTPMockServer {

  private final WireMockServer wireMockServer;

  public HTTPMockServer(int port) {
    wireMockServer = new WireMockServer(wireMockConfig().port(port).extensions(new DelegateToFlowTransformer()));
    wireMockServer.start();
  }

  public void invalidate() {
    wireMockServer.stop();
  }

  // TODO: Remove!!
  public WireMockServer mock() {
    return wireMockServer;
  }
}
