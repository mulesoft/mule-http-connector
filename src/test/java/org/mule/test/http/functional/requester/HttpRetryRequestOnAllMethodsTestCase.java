/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.extension.http.internal.HttpConnectorConstants.RETRY_ON_ALL_METHODS_PROPERTY;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;

public class HttpRetryRequestOnAllMethodsTestCase extends HttpRetryRequestTestCase {

  @Rule
  public SystemProperty retryOnAllMethods = new SystemProperty(RETRY_ON_ALL_METHODS_PROPERTY, "true");

  public HttpRetryRequestOnAllMethodsTestCase(Integer retryAttempts) {
    super(retryAttempts);
  }

  @Override
  protected int getIdempotentMethodExpectedRetries() {
    return retryAttempts;
  }
}
