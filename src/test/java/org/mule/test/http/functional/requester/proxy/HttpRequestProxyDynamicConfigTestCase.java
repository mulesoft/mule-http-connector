/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester.proxy;

import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.api.HttpHeaders.Names.PROXY_AUTHORIZATION;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.http.TestProxyServer;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HttpRequestProxyDynamicConfigTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort firstProxyPort = new DynamicPort("firstProxyPort");
  private TestProxyServer firstProxyServer = new TestProxyServer(firstProxyPort.getNumber(), httpPort.getNumber(), false);

  @Rule
  public DynamicPort secondProxyPort = new DynamicPort("secondProxyPort");
  private TestProxyServer secondProxyServer = new TestProxyServer(secondProxyPort.getNumber(), httpPort.getNumber(), false);

  public String flowName = "theFlow";

  @Override
  protected String getConfigFile() {
    return "http-request-proxy-dynamic-config.xml";
  }

  @Before
  public void startMockProxy() throws Exception {
    firstProxyServer.start();
    secondProxyServer.start();
  }

  @After
  public void stopMockProxy() throws Exception {
    secondProxyServer.stop();
    firstProxyServer.stop();
  }

  @Test
  public void portChangesDependingOnVariable() throws Exception {
    flowRunner(flowName).withVariable("proxyPort", firstProxyPort.getValue()).withPayload(TEST_MESSAGE).keepStreamsOpen().run();
    assertThat(firstProxyServer.hasConnections(), is(true));
    assertThat(secondProxyServer.hasConnections(), is(false));

    flowRunner(flowName).withVariable("proxyPort", secondProxyPort.getValue()).withPayload(TEST_MESSAGE).keepStreamsOpen().run();
    assertThat(firstProxyServer.hasConnections(), is(true));
    assertThat(secondProxyServer.hasConnections(), is(true));
  }

  @Test
  public void dynamicAuthParameters() throws Exception {
    sendRequestAndCheckAuthorization("user1", "password1", "Basic dXNlcjE6cGFzc3dvcmQx");
    sendRequestAndCheckAuthorization("user2", "password2", "Basic dXNlcjI6cGFzc3dvcmQy");
  }

  private void sendRequestAndCheckAuthorization(String name, String password, String authorization) throws Exception {
    CoreEvent event = flowRunner("basicAuthProxy")
        .withPayload(TEST_MESSAGE)
        .withVariable("user", name)
        .withVariable("password", password)
        .run();

    String authorizationHeader = valueOf(headers.asMap().get(PROXY_AUTHORIZATION).toArray()[0]);
    assertThat(authorizationHeader, is(authorization));
    assertThat(event.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
  }
}
