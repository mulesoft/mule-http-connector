

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester.proxy;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.http.TestProxyServer;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunnerDelegateTo(Parameterized.class)
public class HttpRequestProxyConfigTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");

  private TestProxyServer proxyServer = new TestProxyServer(proxyPort.getNumber(), httpPort.getNumber(), false);

  @Parameter()
  public String flowName;

  @Parameter(1)
  public ProxyType proxyType;

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {{"RefAnonymousProxy", ProxyType.ANONYMOUS}, {"InnerAnonymousProxy", ProxyType.ANONYMOUS},
        {"RefUserPassProxy", ProxyType.USER_PASS}, {"InnerUserPassProxy", ProxyType.USER_PASS}, {"RefNtlmProxy", ProxyType.NTLM},
        {"InnerNtlmProxy", ProxyType.NTLM}});
  }

  @Override
  protected String getConfigFile() {
    return "http-request-proxy-config.xml";
  }

  @Before
  public void startMockProxy() throws Exception {
    proxyServer.start();
  }

  @After
  public void stopMockProxy() throws Exception {
    proxyServer.stop();
  }

  @Test
  public void testProxy() throws Exception {
    ensureRequestGoesThroughProxy(flowName);
  }

  private void ensureRequestGoesThroughProxy(String flowName) throws Exception {
    // Request should go through the proxy.
    CoreEvent event = flowRunner(flowName).withPayload(TEST_MESSAGE).keepStreamsOpen().run();

    assertThat(event.getMessage(), hasPayload(equalTo(DEFAULT_RESPONSE)));
    assertThat(proxyServer.hasConnections(), is(true));
  }

  private enum ProxyType {
    ANONYMOUS, USER_PASS, NTLM
  }

}
