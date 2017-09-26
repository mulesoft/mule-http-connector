/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.TIMEOUT;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.qameta.allure.Feature;
import org.eclipse.jetty.server.Request;
import org.junit.Rule;

import org.mule.tck.junit4.rule.SystemProperty;

@Feature(TIMEOUT)
public abstract class AbstractHttpRedirectTimeoutTestCase extends AbstractHttpRequestTestCase {

  private String REDIRECT_URL = format("http://localhost:%s/%s", httpPort.getNumber(), "secondPath");

  @Rule
  public SystemProperty timeoutProperty;

  private long delay;

  public AbstractHttpRedirectTimeoutTestCase(long timeout, long delay) {
    timeoutProperty = new SystemProperty("timeout", valueOf(timeout));
    this.delay = delay;
  }

  protected String getConfigFile() {
    return "http-redirect-timeout-config.xml";
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getRequestURI().contains("firstPath")) {
      response.setStatus(302);
      response.setHeader("Location", REDIRECT_URL);
    } else if (request.getRequestURI().contains("secondPath")) {
      try {
        Thread.sleep(delay);
        response.setContentType("text/plain");
        response.getWriter().print("OK");
      } catch (InterruptedException e) {
        // Ignore interrupted exception.
      }
    }
  }
}
