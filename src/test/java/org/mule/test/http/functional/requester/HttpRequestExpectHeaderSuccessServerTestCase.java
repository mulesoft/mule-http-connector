/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.EXPECTATION_FAILED;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.test.http.functional.AbstractHttpExpectHeaderServerTestCase;
import org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers;
import org.mule.test.runner.RunnerDelegateTo;

@RunnerDelegateTo(Parameterized.class)
public class HttpRequestExpectHeaderSuccessServerTestCase extends AbstractHttpExpectHeaderServerTestCase {

  private static final String REQUEST_FLOW_NAME = "requestFlow";

  private static final String REQUEST_FLOW_NAME_WITHOUT_HEADERS = "requestFlowNoHeaders";

  private boolean persistentConnection;

  public HttpRequestExpectHeaderSuccessServerTestCase(boolean persistentConnection) {
    super(persistentConnection);
    this.persistentConnection = persistentConnection;
  }

  @Override
  protected String getConfigFile() {
    return "http-request-expect-success-header-config.xml";
  }

  @Parameterized.Parameters
  public static List<Object> getParameters() {
    return Arrays.asList(new Object[] {true, false});
  }

  @Test
  public void handlesContinueResponse() throws Exception {
    doHandleRequestResponseIn(REQUEST_FLOW_NAME);
  }

  @Test
  public void handlesContinueResponseWithoutRequestInHeaderField() throws Exception {
    doHandleRequestResponseIn(REQUEST_FLOW_NAME_WITHOUT_HEADERS);
  }


  private void doHandleRequestResponseIn(String flow) throws Exception {
    startExpectContinueServer(persistentConnection);
    final HttpRequestAttributes reqAttributes = mock(HttpRequestAttributes.class);

    flowRunner(flow).withAttributes(reqAttributes).withPayload(TEST_MESSAGE).run();
    assertThat(requestBody, equalTo(TEST_MESSAGE));

    stopServer();
  }

}
