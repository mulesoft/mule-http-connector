/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.proxy;

import static org.mule.runtime.http.api.HttpConstants.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.PROXY;

import static java.lang.Thread.currentThread;

import static org.apache.http.entity.ContentType.DEFAULT_TEXT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpExpectHeaderServerTestCase;

import java.io.IOException;

import io.qameta.allure.Story;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Rule;
import org.junit.Test;

@Story(PROXY)
public class HttpProxyExpectHeaderTestCase extends AbstractHttpExpectHeaderServerTestCase {

  public static boolean getThread() {
    return currentThread().getName().startsWith("[MuleRuntime].io");
  }

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");

  @Override
  protected String getConfigFile() {
    return "http-proxy-template-config.xml";
  }

  @Test
  public void handlesContinueResponse() throws Exception {
    startExpectContinueServer();
    Response response = sendRequest();
    assertThat(response.returnContent().asString(), equalTo(TEST_MESSAGE));
    stopServer();
  }

  @Test
  public void handlesExpectationFailedResponse() throws Exception {
    startExpectFailedServer();
    Response response = sendRequest();
    assertThat(response.returnResponse().getStatusLine().getStatusCode(), is(INTERNAL_SERVER_ERROR.getStatusCode()));
    stopServer();
  }

  private Response sendRequest() throws IOException {
    return Request.Post(String.format("http://localhost:%s", proxyPort.getNumber())).useExpectContinue()
        .bodyString(TEST_MESSAGE, DEFAULT_TEXT).connectTimeout(RECEIVE_TIMEOUT).execute();
  }

}
