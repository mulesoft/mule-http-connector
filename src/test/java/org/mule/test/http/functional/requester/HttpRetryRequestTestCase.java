/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.api.HttpMessageBuilder.refreshSystemProperties;
import static org.mule.extension.http.api.HttpConnectorConstants.DEFAULT_RETRY_ATTEMPTS;
import static org.mule.extension.http.api.HttpConnectorConstants.REMOTELY_CLOSED;
import static org.mule.extension.http.api.HttpConnectorConstants.RETRY_ATTEMPTS_PROPERTY;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.RETRY_POLICY;

import org.mule.functional.api.flow.FlowRunner;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.http.utils.TestServerSocket;
import org.mule.test.runner.RunnerDelegateTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.qameta.allure.Story;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Story(RETRY_POLICY)
@RunnerDelegateTo(Parameterized.class)
public class HttpRetryRequestTestCase extends AbstractHttpTestCase {

  private static ThrowableMessageMatcher REMOTELY_CLOSE_CAUSE_MATCHER =
      new ThrowableMessageMatcher<>(containsString(REMOTELY_CLOSED));

  @Rule
  public SystemProperty retryAttemptsSystemProperty;

  @Rule
  public DynamicPort port = new DynamicPort("httpPort");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  protected int retryAttempts;

  public HttpRetryRequestTestCase(Integer retryAttempts) {
    this.retryAttempts = retryAttempts;
    retryAttemptsSystemProperty = new SystemProperty(RETRY_ATTEMPTS_PROPERTY, retryAttempts.toString());
  }

  @Parameters
  public static Object[] data() {
    int notRetryRequests = 0;
    int customRetryRequests = 2;
    return new Object[] {notRetryRequests, customRetryRequests, DEFAULT_RETRY_ATTEMPTS};
  }

  @Override
  protected String getConfigFile() {
    return "http-retry-policy-config.xml";
  }

  @Before
  public void setUp() {
    refreshSystemProperties();
  }

  @After
  public void tearDown() {
    refreshSystemProperties();
  }

  protected int getIdempotentMethodExpectedRetries() {
    return 0;
  }

  @Test
  public void nonIdempotentMethod() throws Exception {
    FlowRunner flowRunner = flowRunner("retryFlow").withVariable("httpMethod", "POST");
    runRetryPolicyTest(flowRunner, getIdempotentMethodExpectedRetries());
  }

  @Test
  public void idempotentMethod() throws Exception {
    FlowRunner flowRunner = flowRunner("retryFlow").withVariable("httpMethod", "GET");
    runRetryPolicyTest(flowRunner, retryAttempts);
  }

  @Test
  public void entityNotSupportRetry() throws Exception {
    InputStream in = new FileInputStream(new File("src/test/resources/http-retry-policy-payload.json"));
    FlowRunner flowRunner = flowRunner("testRetryPolicyWithPayload").withVariable("httpMethod", "PUT").withPayload(in);
    runRetryPolicyTest(flowRunner, 0);
  }

  @Test
  public void entitySupportRetry() throws Exception {
    FlowRunner flowRunner = flowRunner("testRetryPolicyWithPayload").withVariable("httpMethod", "PUT")
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE);
    runRetryPolicyTest(flowRunner, retryAttempts);
  }

  private void runRetryPolicyTest(FlowRunner flowRunner, int expectedConnections) throws Exception {
    TestServerSocket testServerSocket = new TestServerSocket(port.getNumber(), expectedConnections + 1);
    assertThat("Http server can't be initialized.", testServerSocket.startServer(5000), is(true));
    expectedException.expectCause(REMOTELY_CLOSE_CAUSE_MATCHER);
    try {
      flowRunner.run();
    } finally {
      assertThat(testServerSocket.getConnectionCounter() - 1, is(expectedConnections));
      assertThat("There was an error trying to dispose the http server.", testServerSocket.dispose(5000), is(true));
    }
  }

}
