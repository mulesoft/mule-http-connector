/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;

import org.junit.Test;

public class HttpRequestHostTestCase extends AbstractHttpRequestTestCase {

  @Override
  protected String getConfigFile() {
    return "http-request-host-name-config.xml";
  }

  @Test
  public void sendRequestHostStartsWithWhiteSpace() throws Exception {
    CoreEvent response = flowRunner("hostNameWithSpacesFlow").withPayload(TEST_PAYLOAD).run();
    assertThat(response.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }
}
