/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.streaming;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.mule.tck.http.TestProxyServer;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HttpRequestResponseStreamingWithProxyTestCase extends AbstractHttpRequestResponseStreamingTestCase {

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");

  private final TestProxyServer proxyServer = new TestProxyServer(proxyPort.getNumber(), httpPort.getNumber(), false);

  @Rule
  public SystemProperty streamingProperty = new SystemProperty("streaming", "true");

  @Before
  public void startMockProxy() throws Exception {
    proxyServer.start();
  }

  @After
  public void stopMockProxy() throws Exception {
    proxyServer.stop();
  }

  @Override
  protected String getConfigFile() {
    return "http-request-response-streaming-proxy-config.xml";
  }

  @Test
  public void executionIsExpeditedWhenStreaming() throws Exception {
    flowRunner("client").dispatchAsync(muleContext.getSchedulerService()
        .ioScheduler(
                     muleContext.getSchedulerBaseConfig().withShutdownTimeout(0, SECONDS)));
    pollingProber.check(processorExecuted);
    latch.release();
  }
}
