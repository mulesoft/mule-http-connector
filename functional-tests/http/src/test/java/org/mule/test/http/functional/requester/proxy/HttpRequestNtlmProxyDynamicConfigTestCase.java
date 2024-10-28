/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.proxy;

import static org.mule.runtime.http.api.HttpHeaders.Names.PROXY_AUTHENTICATE;
import static org.mule.runtime.http.api.HttpHeaders.Names.PROXY_AUTHORIZATION;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.NTLM;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.PROXY;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;

import static javax.servlet.http.HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeFalse;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.test.http.functional.requester.ntlm.AbstractAuthNtlmTestCase;
import org.mule.test.http.functional.requester.ntlm.AbstractNtlmTestCase;

import io.qameta.allure.Description;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.BeforeClass;


@Stories({@Story(PROXY), @Story(NTLM)})
public class HttpRequestNtlmProxyDynamicConfigTestCase extends AbstractAuthNtlmTestCase {

  private static final String PROXY_AUTHENTICATION_REQUIRED = "Proxy Authentication Required";


  @BeforeClass
  public static void before() {
    assumeFalse("Ntlm is based on MD5. So this should not run on FIPS", isFipsTesting());
  }

  @Before
  public void setup() {
    setupTestAuthorizer(PROXY_AUTHORIZATION, PROXY_AUTHENTICATE, SC_PROXY_AUTHENTICATION_REQUIRED);
  }

  @Override
  protected String getConfigFile() {
    return "http-request-ntlm-proxy-dynamic-config.xml";
  }

  @Override
  @Description("Verifies that NTLM Auth is successfully performed using dynamic configs.")
  public void validNtlmAuth()
      throws Exception {
    CoreEvent response = flowRunner("ntlmFlowWithCorrectPassword").withVariable("ntlmPassword", "Beeblebrox").run();
    assertThat(response.getMessage().getPayload().getValue(), equalTo(AbstractNtlmTestCase.AUTHORIZED));

    CoreEvent unauthorizedResponse = flowRunner("ntlmFlowWithWrongPassword").withVariable("ntlmPassword", "wrongPassword").run();
    HttpResponseAttributes httpResponseAttributes =
        (HttpResponseAttributes) unauthorizedResponse.getMessage().getAttributes().getValue();
    assertThat(httpResponseAttributes.getStatusCode(), is(SC_PROXY_AUTHENTICATION_REQUIRED));
    assertThat(httpResponseAttributes.getReasonPhrase(), is(PROXY_AUTHENTICATION_REQUIRED));
  }
}
