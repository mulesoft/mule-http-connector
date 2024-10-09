/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.extension.http.api.HttpMessageBuilder.refreshSystemProperties;
import static org.mule.extension.http.internal.HttpConnectorConstants.RETRY_ATTEMPTS_PROPERTY;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.RETRY_POLICY;

import static org.eclipse.jetty.server.HttpConnection.getCurrentConnection;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.qameta.allure.Story;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@Story(RETRY_POLICY)
public class HttpRetryRequestStreamingPayloadTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort httpPortPayload = new DynamicPort("httpPortPayload");

  @Rule
  public SystemProperty retryAttemptsSystemProperty = new SystemProperty(RETRY_ATTEMPTS_PROPERTY, "1");

  private volatile int requestsCount = 0;
  private volatile String receivedBody;

  @Override
  protected String getConfigFile() {
    return "http-retry-policy-streaming-config.xml";
  }

  @Before
  public void setUp() {
    refreshSystemProperties();
  }

  @After
  public void tearDown() {
    refreshSystemProperties();
  }

  @Test
  public void retryWithRepeatableStreamingPayload() throws Exception {
    flowRunner("retryFlow").withVariable("httpMethod", "PUT").run();
    assertThat(receivedBody, is("I'm gonna be repeatable!! Woo-hoo!"));
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (requestsCount < 1) {
      // Force the retry of the requester
      getCurrentConnection().close();
    } else {
      receivedBody = IOUtils.toString(baseRequest.getInputStream());
      super.handleRequest(baseRequest, request, response);
    }

    requestsCount++;

  }
}
