/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.URL_ENCODED;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import io.qameta.allure.Story;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Rule;
import org.junit.Test;

@Story(URL_ENCODED)
public class HttpListenerUrlEncodedTestCase extends AbstractHttpTestCase {

  private static final String PARAM_1_NAME = "param1";
  private static final String PARAM_2_NAME = "param2";
  private static final String PARAM_1_VALUE = "param1Value";
  private static final String PARAM_2_VALUE = "param2Value";

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");
  @Rule
  public SystemProperty path = new SystemProperty("path", "path");


  @Override
  protected String getConfigFile() {
    return "http-listener-url-encoded-config.xml";
  }


  @Test
  public void receivesUrlEncodedRequest() throws Exception {
    final Response response = Request.Post(getListenerUrl("receive"))
        .bodyForm(new BasicNameValuePair(PARAM_1_NAME, PARAM_1_VALUE), new BasicNameValuePair(PARAM_2_NAME, PARAM_2_VALUE))
        .execute();

    assertThat(response.returnContent().asString(), is(PARAM_1_VALUE + PARAM_2_VALUE));
  }

  @Test
  public void sendUrlEncodedResponse() throws Exception {
    final Response response = Request.Get(getListenerUrl("send")).execute();

    assertThat(response.returnContent().asString(), is("testName1=testValue1&testName2=testValue2"));
  }

  private String getListenerUrl(String path) {
    return String.format("http://localhost:%s/%s", listenPort.getNumber(), path);
  }

}
