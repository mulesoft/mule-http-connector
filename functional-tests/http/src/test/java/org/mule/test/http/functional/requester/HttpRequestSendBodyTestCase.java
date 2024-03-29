/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class HttpRequestSendBodyTestCase extends AbstractHttpRequestTestCase {

  @Override
  protected String getConfigFile() {
    return "http-request-send-body-config.xml";
  }

  @Test
  public void defaultsToConfigSendBodyModeIfEmpty() throws Exception {
    assertEmptyBody("sendBodyConfig", TEST_MESSAGE, "POST");
  }

  @Test
  public void sendBodyAutoSendsPayloadPost() throws Exception {
    assertNotEmptyBody("sendBodyAuto", TEST_MESSAGE, "POST");
  }

  @Test
  public void sendBodyAutoIgnoresPayloadGet() throws Exception {
    assertEmptyBody("sendBodyAuto", TEST_MESSAGE, "GET");
  }

  @Test
  public void sendBodyAutoIgnoresNullPayloadPost() throws Exception {
    assertEmptyBody("sendBodyAuto", null, "POST");
  }

  @Test
  public void sendBodyNeverIgnoresPayloadPost() throws Exception {
    assertEmptyBody("sendBodyNever", TEST_MESSAGE, "POST");
  }

  @Test
  public void sendBodyNeverIgnoresNullPayloadPost() throws Exception {
    assertEmptyBody("sendBodyNever", null, "POST");
  }

  @Test
  public void sendBodyAlwaysSendsPayloadGet() throws Exception {
    assertNotEmptyBody("sendBodyAlways", TEST_MESSAGE, "GET");
  }

  @Test
  public void sendBodyAlwaysIgnoresNullPayloadGet() throws Exception {
    assertEmptyBody("sendBodyAlways", null, "GET");
  }

  private void assertEmptyBody(String flowName, Object payload, String method) throws Exception {
    flowRunner(flowName).withPayload(payload).withVariable("method", method).run();

    assertThat(body, equalTo(""));
    if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
      assertThat(getFirstReceivedHeader("Content-Length"), is(nullValue()));
    } else {
      assertThat(getFirstReceivedHeader("Content-Length"), is("0"));
    }
  }

  private void assertNotEmptyBody(String flowName, Object payload, String method) throws Exception {
    flowRunner(flowName).withPayload(payload).withVariable("method", method).run();

    assertThat(body, equalTo(TEST_MESSAGE));
    assertThat(headers.containsKey("Content-Length"), is(true));
    assertThat(getFirstReceivedHeader("Content-Length"), is(String.valueOf(TEST_MESSAGE.length())));
  }
}
