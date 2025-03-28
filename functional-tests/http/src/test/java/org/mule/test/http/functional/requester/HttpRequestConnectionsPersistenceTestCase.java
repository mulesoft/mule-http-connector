/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.probe.JUnitProbe;
import org.mule.tck.probe.PollingProber;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Request;

import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpRequestConnectionsPersistenceTestCase extends AbstractHttpRequestTestCase {

  private static final int GRIZZLY_IDLE_CHECK_TIMEOUT_MILLIS = 6000;
  private static final int POLL_DELAY_MILLIS = 200;
  private static final int SMALL_TIMEOUT_MILLIS = 500;
  private static final int SMALL_POLL_DELAY_MILLIS = 100;
  private int remotePort;
  private JUnitProbe probe = new JUnitProbe() {

    @Override
    public boolean test() throws Exception {
      return getConnectedEndPoint() == null;
    }

    @Override
    public String describeFailure() {
      return "Connection should be closed.";
    }
  };

  @Override
  protected String getConfigFile() {
    return "http-request-connections-persistence-config.xml";
  }

  @Test
  public void persistentConnections() throws Exception {
    flowRunner("persistent").withPayload(TEST_MESSAGE).run();
    ensureConnectionIsOpen();

    new PollingProber(GRIZZLY_IDLE_CHECK_TIMEOUT_MILLIS, POLL_DELAY_MILLIS).check(probe);
  }

  @Test
  public void nonPersistentConnections() throws Exception {
    CoreEvent response = flowRunner("nonPersistent").keepStreamsOpen().run();
    // verify that the connection is released shortly
    new PollingProber(SMALL_TIMEOUT_MILLIS, SMALL_POLL_DELAY_MILLIS).check(probe);
    // verify the stream is still available
    assertThat(response.getMessage(), hasPayload(is(DEFAULT_RESPONSE)));
  }

  private void ensureConnectionIsOpen() {
    EndPoint endPoint = getConnectedEndPoint();

    assertThat(endPoint, is(notNullValue()));

    assertThat(endPoint.getLocalAddress().getPort(), is(httpPort.getNumber()));
    assertThat(endPoint.getRemoteAddress().getPort(), is(remotePort));
  }

  private EndPoint getConnectedEndPoint() {
    assertThat(server.getConnectors().length, is(1));

    Collection<EndPoint> connectedEndpoints = server.getConnectors()[0].getConnectedEndPoints();

    if (!connectedEndpoints.isEmpty()) {
      return connectedEndpoints.iterator().next();
    }
    return null;
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    super.handleRequest(baseRequest, request, response);
    remotePort = request.getRemotePort();
  }


}


