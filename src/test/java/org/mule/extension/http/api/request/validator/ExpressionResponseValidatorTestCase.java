/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;

import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

public class ExpressionResponseValidatorTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  @Override
  protected String getConfigFile() {
    return "http-request-response-validator-config.xml";
  }

  @Test
  public void payloadStreamIsManaged() throws Exception {
    assertThat(flowRunner("repeatableStreamingFlow").keepStreamsOpen().run().getMessage(),
               hasPayload(equalTo(DEFAULT_RESPONSE)));
  }
}
