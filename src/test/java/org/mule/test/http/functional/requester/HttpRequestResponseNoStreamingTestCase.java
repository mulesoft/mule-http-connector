/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.extension.http.internal.HttpConnectorConstants.DISABLE_RESPONSE_STREAMING_PROPERTY;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.STREAMING;

import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(HTTP_EXTENSION)
@Story(STREAMING)
public class HttpRequestResponseNoStreamingTestCase extends AbstractHttpRequestResponseStreamingTestCase {

  @Rule
  public SystemProperty noStreaming = new SystemProperty(DISABLE_RESPONSE_STREAMING_PROPERTY, "true");

  @Override
  protected String getConfigFile() {
    return "http-request-response-streaming-config.xml";
  }

  @Test
  public void executionHangsWhenNotStreaming() throws Exception {
    flowRunner("client").dispatchAsync();
    pollingProber.check(processorNotExecuted);
    latch.release();
    pollingProber.check(processorExecuted);
  }

}
