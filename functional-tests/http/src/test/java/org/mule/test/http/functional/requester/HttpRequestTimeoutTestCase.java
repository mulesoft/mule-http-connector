/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;


import static org.mule.functional.junit4.matchers.ThrowableMessageMatcher.hasMessage;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertTrue;

import org.mule.runtime.api.util.concurrent.Latch;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Request;

import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpRequestTimeoutTestCase extends AbstractHttpRequestTestCase {

  private static final int TEST_TIMEOUT = 5000;

  private final Latch serverLatch = new Latch();

  @Override
  protected String getConfigFile() {
    return "http-request-timeout-config.xml";
  }

  @Test
  public void throwsExceptionWhenRequesterTimeoutIsExceeded() throws Exception {
    assertTimeout("requestFlow", 1);
  }

  private void assertTimeout(final String flowName, final int responseTimeoutRequester)
      throws Exception {
    final Latch requestTimeoutLatch = new Latch();

    Thread thread = new Thread() {

      @Override
      public void run() {
        try {
          flowRunner(flowName).withPayload(TEST_MESSAGE).withVariable("timeout", responseTimeoutRequester)
              .runExpectingException(hasMessage(containsString("Timeout exceeded")));
          requestTimeoutLatch.release();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };

    thread.start();

    // Wait for the request to timeout (the thread sending the request will release the latch.
    assertTrue(requestTimeoutLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS));
    thread.join();

    // Release the server latch so that the server thread can finish.
    serverLatch.release();
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      // Block until the end of the test in order to make the request timeout.
      serverLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
