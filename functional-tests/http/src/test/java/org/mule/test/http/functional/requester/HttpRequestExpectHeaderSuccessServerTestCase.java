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

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.test.http.functional.AbstractHttpExpectHeaderServerTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized;

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
  @Ignore
  public void handlesContinueResponse() throws Exception {
    doHandleRequestResponseIn(REQUEST_FLOW_NAME);
  }

  @Test
  @Ignore
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
