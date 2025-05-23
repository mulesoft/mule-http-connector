/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AnyOf.anyOf;

import org.mule.extension.http.api.request.validator.ResponseValidatorException;
import org.mule.extension.http.api.request.validator.ResponseValidatorTypedException;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.io.IOException;

import org.eclipse.jetty.server.Request;

import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpRequestStatusCodesTestCase extends AbstractHttpRequestTestCase {

  @Override
  protected String getConfigFile() {
    return "http-request-status-codes-config.xml";
  }

  @Test
  public void defaultStatusCodeValidatorSuccess() throws Exception {
    assertSuccess(200, "default");
  }

  @Test
  public void defaultStatusCodeValidatorFailure() throws Exception {
    assertFailure(500, "default");
  }

  @Test
  public void successStatusCodeValidatorSuccess() throws Exception {
    assertSuccess(409, "success");
  }

  @Test
  public void successStatusCodeValidatorFailure() throws Exception {
    assertFailure(200, "success");
  }

  @Test
  public void failureStatusCodeValidatorSuccess() throws Exception {
    assertSuccess(200, "failure");
  }

  @Test
  public void failureStatusCodeValidatorFailure() throws Exception {
    assertFailure(201, "failure");
  }

  private void assertSuccess(int statusCode, String flowName) throws Exception {
    flowRunner(flowName).withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).withVariable("code", toString(statusCode)).run();
  }

  private String toString(int statusCode) {
    return Integer.valueOf(statusCode).toString();
  }

  private void assertFailure(int statusCode, String flowName) throws Exception {
    flowRunner(flowName).withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).withVariable("code", toString(statusCode))
        .runExpectingException(anyOf(instanceOf(ResponseValidatorException.class),
                                     instanceOf(ResponseValidatorTypedException.class)));
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    int statusCode = Integer.parseInt(request.getParameter("code"));

    response.setContentType("text/html");
    response.setStatus(statusCode);
    response.getWriter().print(DEFAULT_RESPONSE);
  }
}
