/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class HttpRequestResponseStreamingTestCase extends AbstractHttpRequestResponseStreamingTestCase {

  @Rule
  public SystemProperty streamingProperty = new SystemProperty("streaming", "true");

  @Override
  protected String getConfigFile() {
    return "http-request-response-streaming-config.xml";
  }

  @Test
  public void executionIsExpeditedWhenStreaming() throws Exception {
    flowRunner("client").dispatchAsync(muleContext.getSchedulerService()
        .ioScheduler(muleContext.getSchedulerBaseConfig().withShutdownTimeout(0, SECONDS)));
    pollingProber.check(processorExecuted);
    latch.release();
  }

}
