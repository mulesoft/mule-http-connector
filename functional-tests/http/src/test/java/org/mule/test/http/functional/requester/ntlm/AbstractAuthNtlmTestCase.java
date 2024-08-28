/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.ntlm;


import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.NTLM;
import static org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers.hasStatusCode;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.message.Message;

import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.junit.Test;

@Story(NTLM)
public abstract class AbstractAuthNtlmTestCase extends AbstractNtlmTestCase {

  private static final String AUTHORIZED = "Authorized";

  @Test
  @Description("Verifies a flow involving a NTLM proxy is successfully performed.")
  public void validNtlmAuth() throws Exception {
    Message response = runFlow(getFlowName()).getMessage();

    assertThat((HttpResponseAttributes) response.getAttributes().getValue(), hasStatusCode(SC_OK));
    assertThat(getPayloadAsString(response), equalTo(AUTHORIZED));
  }

  protected String getFlowName() {
    return "ntlmFlow";
  }

}
