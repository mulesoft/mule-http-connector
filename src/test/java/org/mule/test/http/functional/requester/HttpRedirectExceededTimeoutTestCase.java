/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.isA;
import static org.mule.extension.http.internal.listener.HttpListener.HTTP_NAMESPACE;
import static org.mule.extension.http.api.error.HttpError.TIMEOUT;

import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.functional.api.exception.ExpectedError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpRedirectExceededTimeoutTestCase extends AbstractHttpRedirectTimeoutTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public ExpectedError expectedError = ExpectedError.none();


  private static long HTTP_TIMEOUT = 600;

  public HttpRedirectExceededTimeoutTestCase() {
    super(HTTP_TIMEOUT, HTTP_TIMEOUT * 2);
  }

  @Test
  public void testRedirectTimeout() throws Exception {
    expectedError.expectCause(isA(HttpRequestFailedException.class));
    expectedError.expectErrorType(HTTP_NAMESPACE.toUpperCase(), TIMEOUT.getType());
    expectedException.reportMissingExceptionWithMessage("Timeout exception must be triggered");
    runFlow("requestFlow");
  }

}
