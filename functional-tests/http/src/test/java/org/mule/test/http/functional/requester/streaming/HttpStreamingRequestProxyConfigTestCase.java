/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.streaming;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.PROXY;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.STREAMING;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.http.TestProxyServer;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.util.Collection;

import io.qameta.allure.Issue;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@Issue("W-16606364")
@Stories({@Story(PROXY), @Story(STREAMING)})
@RunnerDelegateTo(Parameterized.class)
public class HttpStreamingRequestProxyConfigTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");

  @Rule
  public DynamicPort port = new DynamicPort("port");

  private final TestProxyServer proxyServer = new TestProxyServer(proxyPort.getNumber(), httpPort.getNumber(), false);

  @Parameter
  public String flowName;

  @Parameters
  public static Collection<String> parameters() {
    return asList("RefAnonymousProxy", "InnerAnonymousProxy", "RefUserPassProxy", "InnerUserPassProxy");
  }

  @Override
  protected String getConfigFile() {
    return "http-streaming-request-proxy-config.xml";
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
    CoreEvent event = flowRunner(flowName).withPayload(TEST_MESSAGE).keepStreamsOpen().run();

    assertThat(event.getMessage(), hasPayload(equalTo(DEFAULT_RESPONSE)));
    assertThat(proxyServer.hasConnections(), is(true));
  }
}
