/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.proxy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.http.TestProxyServer;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HttpRequestProxyHostsTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");

  private TestProxyServer proxyServer = new TestProxyServer(proxyPort.getNumber(), httpPort.getNumber(), false);

  @Override
  protected String getConfigFile() {
    return "http-request-proxy-params-config.xml";
  }

  @Before
  public void setUp() throws Exception {
    proxyServer.start();
  }

  @After
  public void tearDown() throws Exception {
    proxyServer.stop();
  }

  @Test
  public void proxyWithNonProxyHostsParam() throws Exception {
    verifyResponseUsingProxy("nonProxyParamProxy", false);
  }

  @Test
  public void innerProxyWithNonProxyHostsParam() throws Exception {
    verifyResponseUsingProxy("innerNonProxyParamProxy", false);
  }

  @Test
  public void proxyWithMultipleHostsNonProxyHostsParam() throws Exception {
    verifyResponseUsingProxy("innerNonProxyParamProxyMultipleHosts", false);
  }

  @Test
  public void proxyWithoutNonProxyHostsParam() throws Exception {
    verifyResponseUsingProxy("refAnonymousProxy", true);
  }

  @Test
  public void proxyWithAnotherHostNonProxyHostsParam() throws Exception {
    verifyResponseUsingProxy("innerNonProxyParamProxyAnotherHost", true);
  }

  @Test
  public void noProxy() throws Exception {
    verifyResponseUsingProxy("noProxy", false);
  }

  private void verifyResponseUsingProxy(String flowName, boolean throughProxy) throws Exception {
    final CoreEvent event = runFlow(flowName);
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(OK.getStatusCode()));
    assertThat(event.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
    assertThat(proxyServer.hasConnections(), is(throughProxy));
  }
}
