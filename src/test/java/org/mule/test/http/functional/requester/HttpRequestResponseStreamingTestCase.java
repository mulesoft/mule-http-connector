/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.STREAMING;

import org.junit.Test;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

@Features(HTTP_EXTENSION)
@Stories(STREAMING)
public class HttpRequestResponseStreamingTestCase extends AbstractHttpRequestResponseStreamingTestCase {

  @Override
  protected String getConfigFile() {
    return "http-request-response-streaming-config.xml";
  }

  @Test
  public void executionIsExpeditedWhenStreaming() throws Exception {
    flowRunner("client").dispatchAsync();
    pollingProber.check(processorExecuted);
    latch.release();
  }

}
