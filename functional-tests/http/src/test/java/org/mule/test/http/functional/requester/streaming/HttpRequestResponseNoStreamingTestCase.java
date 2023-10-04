/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.streaming;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class HttpRequestResponseNoStreamingTestCase extends AbstractHttpRequestResponseStreamingTestCase {

  @Rule
  public SystemProperty streamingProperty = new SystemProperty("streaming", "false");

  @Override
  protected String getConfigFile() {
    return "http-request-response-streaming-config.xml";
  }

  @Test
  public void executionHangsWhenNotStreaming() throws Exception {
    flowRunner("client").dispatchAsync(muleContext.getSchedulerService()
        .ioScheduler(muleContext.getSchedulerBaseConfig().withShutdownTimeout(0, SECONDS)));
    pollingProber.check(processorNotExecuted);
    latch.release();
    pollingProber.check(processorExecuted);
  }

}
