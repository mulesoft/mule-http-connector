/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.redirect;

import static com.ning.http.client.AsyncHttpClientConfigDefaults.ASYNC_CLIENT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class HttpRedirectTimeoutTestCase extends AbstractHttpRedirectTimeoutTestCase {

  private static final String GLOBAL_TIMEOUT = "300";

  private static final String GLOBAL_REQUEST_TIMEOUT = ASYNC_CLIENT + "requestTimeout";

  private static final long TIMEOUT = 600;

  private static final long DELAY = 450;

  @Rule
  public SystemProperty globalRequestTimeoutSystemProperty = new SystemProperty(GLOBAL_REQUEST_TIMEOUT, GLOBAL_TIMEOUT);

  public HttpRedirectTimeoutTestCase() {
    super(TIMEOUT, DELAY);
  }

  @Test
  public void testRedirectTimeout() throws Exception {
    CoreEvent event = flowRunner("requestFlow").run();
    assertThat(getPayloadAsString(event.getMessage()), is("OK"));
  }

}
