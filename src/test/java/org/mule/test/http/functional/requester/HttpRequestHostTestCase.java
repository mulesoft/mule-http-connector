/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import org.junit.Test;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.AbstractMuleTestCase;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;

public class HttpRequestHostTestCase extends AbstractHttpRequestTestCase {

  @Override
  protected String getConfigFile() {
    return "http-request-host-name-config.xml";
  }

  @Test
  public void sendRequestHostStartsWithWhiteSpace() throws Exception {
    CoreEvent response = flowRunner("hostNameWithSpacesFlow").withPayload(AbstractMuleTestCase.TEST_PAYLOAD).run();
    assertThat(response.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }
}
