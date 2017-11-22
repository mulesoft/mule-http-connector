/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static java.lang.reflect.Modifier.FINAL;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.internal.request.HttpRequester.DEFAULT_RETRY_ATTEMPTS;
import static org.mule.extension.http.internal.request.HttpRequester.REMOTELY_CLOSED;
import org.mule.extension.http.internal.request.HttpRequester;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.http.utils.TestServerSocket;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Rule;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;

public abstract class AbstractHttpRetryRequestTestCase extends AbstractHttpTestCase {

  private static ThrowableMessageMatcher REMOTELY_CLOSE_CAUSE_MATCHER =
      new ThrowableMessageMatcher<>(containsString(REMOTELY_CLOSED));

  @Rule
  public DynamicPort port = new DynamicPort("httpPort");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Override
  protected String getConfigFile() {
    return "http-retry-policy-config.xml";
  }

  @Before
  public void setUp() throws Exception {
    expectedException.expectCause(REMOTELY_CLOSE_CAUSE_MATCHER);
    Field retryAttemptsField = HttpRequester.class.getDeclaredField("RETRY_ATTEMPTS");
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(retryAttemptsField, retryAttemptsField.getModifiers() & ~FINAL);
    retryAttemptsField.setAccessible(true);
    retryAttemptsField.setInt(null, getNumberOfRetries());
  }

  void runIdempotentFlow() throws Exception {
    runIdempotentFlow(DEFAULT_RETRY_ATTEMPTS);
  }

  void runIdempotentFlow(int numberOfRetryExpected) throws Exception {
    TestServerSocket testServerSocket = new TestServerSocket(port.getNumber(), numberOfRetryExpected + 1);
    assertThat("Http server can't be initialized.", testServerSocket.startServer(5000), is(true));
    try {
      runFlow("retryIdempotentMethod");
    } finally {
      assertThat(testServerSocket.getConnectionCounter() - 1, is(numberOfRetryExpected));
    }
  }

  void runNonIdempotentFlow() throws Exception {
    TestServerSocket testServerSocket = new TestServerSocket(port.getNumber(), 1);
    assertThat("Http server can't be initialized.", testServerSocket.startServer(5000), is(true));
    try {
      runFlow("retryNonIdempotentMethod");
    } finally {
      assertThat(testServerSocket.getConnectionCounter() - 1, is(0));
    }
  }

  protected int getNumberOfRetries() {
    return DEFAULT_RETRY_ATTEMPTS;
  }

}
