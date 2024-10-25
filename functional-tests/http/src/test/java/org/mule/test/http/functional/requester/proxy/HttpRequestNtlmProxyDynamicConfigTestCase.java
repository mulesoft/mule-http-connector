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
import static org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers.hasStatusCode;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;
import static javax.servlet.http.HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeFalse;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers;
import org.mule.test.http.functional.requester.ntlm.AbstractAuthNtlmTestCase;

import io.qameta.allure.Description;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


@Stories({@Story(PROXY), @Story(NTLM)})
public class HttpRequestNtlmProxyDynamicConfigTestCase extends AbstractAuthNtlmTestCase {

  @BeforeClass
  public static void before() {
    assumeFalse("Ntlm is based on MD5. So this should not run on FIPS",
                isFipsTesting());
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
  public void validNtlmAuth() throws Exception {
    Message response = runFlow("ntlmFlowWithCorrectPassword").getMessage();
    assertThat((HttpResponseAttributes) response.getAttributes().getValue(), hasStatusCode(SC_OK));
    Message unauthorizedResponse = runFlow("ntlmFlowWithWrongPassword").getMessage();
    assertThat((HttpResponseAttributes) unauthorizedResponse.getAttributes().getValue(),
               hasStatusCode(SC_PROXY_AUTHENTICATION_REQUIRED));
  }
}
