/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;


import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.http.api.HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED;
import static org.mule.test.allure.AllureConstants.HttpFeature.HttpStory.URL_ENCODED;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.http.api.HttpHeaders;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import io.qameta.allure.Story;
import org.junit.Test;

@Story(URL_ENCODED)
public class HttpRequestUrlEncodedTestCase extends AbstractHttpRequestTestCase {

  private static final String URL_ENCODED_STRING = "testName1=testValue1&testName2=testValue2";

  @Override
  protected String getConfigFile() {
    return "http-request-form-params-config.xml";
  }

  @Test
  public void sendsUrlEncodedBody() throws Exception {
    flowRunner("formParamOutbound").run();

    assertThat(uri, equalTo("/testPath"));
    assertThat(body, equalTo("testName1=testValue1&testName2=testValue2"));
    assertThat(getFirstReceivedHeader(HttpHeaders.Names.CONTENT_TYPE),
               startsWith(APPLICATION_X_WWW_FORM_URLENCODED.toRfcString()));
  }

  @Test
  public void receivesUrlEncodedBody() throws Exception {
    CoreEvent event = flowRunner("formParamInbound").withPayload(TEST_MESSAGE).run();

    assertThat(event.getMessage(), hasPayload(equalTo("testValue1testValue2")));
  }

  @Override
  protected void writeResponse(HttpServletResponse response) throws IOException {
    response.setContentType(APPLICATION_X_WWW_FORM_URLENCODED.toRfcString());
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().print(URL_ENCODED_STRING);
  }
}
