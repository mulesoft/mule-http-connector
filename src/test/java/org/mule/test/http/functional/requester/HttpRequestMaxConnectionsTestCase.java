/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.mule.functional.junit4.matchers.ThrowableMessageMatcher.hasMessage;
import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.construct.Flow;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.Test;

public class HttpRequestMaxConnectionsTestCase extends AbstractHttpRequestTestCase {

  private Latch messageArrived = new Latch();
  private Latch messageHold = new Latch();

  @Inject
  @Named("limitedConnections")
  private Flow limitedConnectionsFlow;

  @Override
  protected String getConfigFile() {
    return "http-request-max-connections-config.xml";
  }

  @Test
  public void maxConnections() throws Exception {
    Thread t1 = processAsynchronously(limitedConnectionsFlow);
    messageArrived.await();

    // Max connections should be reached
    flowRunner("limitedConnections").runExpectingException(allOf(instanceOf(HttpRequestFailedException.class),
                                                                 hasMessage(containsString("No slot available"))));

    messageHold.release();
    t1.join();
  }

  private Thread processAsynchronously(final Flow flow) {
    Thread thread = new Thread(() -> {
      try {
        flowRunner("limitedConnections").run();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    thread.start();
    return thread;
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    super.handleRequest(baseRequest, request, response);
    messageArrived.release();
    try {
      messageHold.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
