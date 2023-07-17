/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;


import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.NTLM;

import org.junit.Test;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers;

import io.qameta.allure.Description;
import io.qameta.allure.Story;

@Story(NTLM)
public abstract class AbstractAuthNtlmTestCase extends AbstractNtlmTestCase {

  private static final String AUTHORIZED = "Authorized";

  @Test
  @Description("Verifies a flow involving a NTLM proxy is successfully performed.")
  public void validNtlmAuth() throws Exception {
    Message response = runFlow(getFlowName()).getMessage();

    assertThat((HttpResponseAttributes) response.getAttributes().getValue(), HttpMessageAttributesMatchers.hasStatusCode(SC_OK));
    assertThat(getPayloadAsString(response), equalTo(AUTHORIZED));
  }

  protected String getFlowName() {
    return "ntlmFlow";
  }

}
