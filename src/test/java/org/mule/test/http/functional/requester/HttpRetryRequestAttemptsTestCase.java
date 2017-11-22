/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static org.junit.runners.Parameterized.Parameters;
import org.mule.test.runner.RunnerDelegateTo;

import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class HttpRetryRequestAttemptsTestCase extends AbstractHttpRetryRequestTestCase {

  private int numberOfRetries;

  @Parameters
  public static Object[] data() {
    int notRetryRequests = 0;
    int customRetryRequests = 2;
    return new Object[] {notRetryRequests, customRetryRequests};
  }

  public HttpRetryRequestAttemptsTestCase(Integer numberOfRetries) {
    this.numberOfRetries = numberOfRetries;
  }

  @Test
  public void customRetryRequestAttemptsIdempotentMethod() throws Exception {
    runIdempotentFlow(numberOfRetries);
  }

  @Test
  public void customRetryRequestAttemptsNonIdempotentMethod() throws Exception {
    runNonIdempotentFlow();
  }

  @Override
  public int getNumberOfRetries() {
    return numberOfRetries;
  }

}
