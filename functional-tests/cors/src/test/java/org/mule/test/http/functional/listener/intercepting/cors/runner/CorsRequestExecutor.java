/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors.runner;

import static org.junit.Assert.fail;

import org.mule.modules.cors.result.KernelTestResult;
import org.mule.test.http.functional.listener.intercepting.cors.parameters.CorsHttpParameters;
import org.mule.test.http.functional.listener.intercepting.cors.result.CorsHttpTestResult;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class CorsRequestExecutor {

  private CloseableHttpClient httpClient = HttpClients.createDefault();

  public KernelTestResult execute(CorsHttpParameters parameters, CorsHttpEndpoint endpoint) {
    try {
      return new CorsHttpTestResult(httpClient.execute(parameters.buildRequest(endpoint.port(), endpoint)));
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
    }
  }

  public void dispose() {
    try {
      httpClient.close();
    } catch (IOException e) {
      fail("Failed to close httpClient: " + e.getMessage());
    }
  }
}
