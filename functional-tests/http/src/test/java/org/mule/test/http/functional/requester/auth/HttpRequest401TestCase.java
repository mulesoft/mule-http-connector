/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.auth;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.Test;

public class HttpRequest401TestCase extends AbstractHttpRequestTestCase {

  private static final String UNAUTHORIZED_MESSAGE = "Unauthorized: check credentials.";

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setStatus(SC_UNAUTHORIZED);
    response.getWriter().print(UNAUTHORIZED_MESSAGE);
  }

  @Override
  protected String getConfigFile() {
    return "http-request-401-config.xml";
  }

  @Test
  public void returns401Response() throws Exception {
    Message response = runFlow("executeRequest").getMessage();
    assertThat((HttpResponseAttributes) response.getAttributes().getValue(), HttpMessageAttributesMatchers
        .hasStatusCode(SC_UNAUTHORIZED));
    assertThat(response.getPayload().getValue(), is(UNAUTHORIZED_MESSAGE));
  }

}
