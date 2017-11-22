/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import org.junit.Test;

public class HttpRetryRequestWithIdempotencyTestCase extends AbstractHttpRetryRequestTestCase {

  @Test
  public void testIdempotentMethod() throws Exception {
    runIdempotentFlow();
  }

  @Test
  public void testNoIdempotentMethod() throws Exception {
    runNonIdempotentFlow();
  }

}
