/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONNECTION;
import static org.mule.runtime.http.api.HttpHeaders.Names.UPGRADE;
import org.mule.runtime.api.message.Message;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.qameta.allure.Description;
import org.eclipse.jetty.server.Request;
import org.junit.Rule;
import org.junit.Test;

public class HttpRequestResponseHeadersTestCase extends AbstractHttpRequestTestCase {

  private static final String EMPTY_PATH = "empty";
  private static final String SIMPLE_PATH = "simple";
  private static final String MULTIPLE_PATH = "multiple";

  @Rule
  public SystemProperty header = new SystemProperty("header", "custom");

  @Override
  protected String getConfigFile() {
    return "http-request-response-headers-config.xml";
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String path = request.getPathInfo().substring(1);
    if (EMPTY_PATH.equals(path)) {
      response.addHeader(header.getValue(), EMPTY);
    } else if (SIMPLE_PATH.equals(path)) {
      response.addHeader(header.getValue(), "custom1");
    } else if (MULTIPLE_PATH.equals(path)) {
      response.addHeader(header.getValue(), "custom1");
      response.addHeader(header.getValue(), "custom2");
    } else if ("responseWithUpgradeToHttp2Header".equals(path)) {
      response.addHeader(UPGRADE, "h2,h2c");
      response.addHeader(CONNECTION, "Upgrade, close");
      super.writeResponse(response);
    }
    super.writeResponse(response);
  }

  @Test
  public void handlesEmptyHeader() throws Exception {
    testHeaders(EMPTY_PATH, EMPTY);
  }

  @Test
  public void handlesSimpleHeader() throws Exception {
    testHeaders(SIMPLE_PATH, "custom1");
  }

  @Test
  public void handlesMultipleHeadersString() throws Exception {
    testHeaders("multipleString", "custom2");
  }

  @Test
  public void handlesMultipleHeadersCollection() throws Exception {
    testHeaders("multipleCollection", "custom1");
  }

  @Test
  @Description("This case is not valid according to the RFC (https://http2.github.io/http2-spec/), but in any case, it shouldn't cause the request to fail.")
  public void responseWithUpgradeToHttp2Header() throws Exception {
    assertThat(flowRunner("responseWithUpgradeToHttp2Header").run(), not(nullValue()));
  }

  private void testHeaders(String flowName, String expectedResponse) throws Exception {
    Message response = flowRunner(flowName).run().getMessage();
    assertThat(response, hasPayload(equalTo(expectedResponse)));
  }
}
