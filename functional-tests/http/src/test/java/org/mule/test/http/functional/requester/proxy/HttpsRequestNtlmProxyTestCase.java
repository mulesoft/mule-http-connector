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

import static org.junit.Assume.assumeFalse;
import static org.hamcrest.MatcherAssert.assertThat;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.test.http.functional.requester.NtlmConnectHandler;
import org.mule.test.http.functional.requester.ntlm.AbstractAuthNtlmTestCase;

import io.qameta.allure.Description;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import io.qameta.allure.junit4.DisplayName;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@Stories({@Story(PROXY), @Story(NTLM)})
@DisplayName("HTTPS server behind NTLM HTTP proxy. Authentication is required.")
public class HttpsRequestNtlmProxyTestCase extends AbstractAuthNtlmTestCase {

  @BeforeClass
  public static void before() {
    assumeFalse("Ntlm is based on MD5. So this should not run on FIPS",
                isFipsTesting());
  }

  @Override
  protected AbstractHandler createHandler(Server server) {
    try {
      setupTestAuthorizer(PROXY_AUTHORIZATION, PROXY_AUTHENTICATE, SC_PROXY_AUTHENTICATION_REQUIRED);
      return new NtlmConnectHandler(getAuthorizer());
    } catch (Exception e) {
      throw new RuntimeException("Error creating testAuthorizer");
    }
  }

  @Before
  public void setup() {
    setupTestAuthorizer(PROXY_AUTHORIZATION, PROXY_AUTHENTICATE, SC_PROXY_AUTHENTICATION_REQUIRED);
  }

  @Override
  protected String getConfigFile() {
    return "https-request-ntlm-proxy-config.xml";
  }

  @Test
  @Description("Verifies that HTTP CONNECT is established for TLS tunnelling through a NTLM Proxy with mandatory auth.")
  public void validNtlmAuth() throws Exception {
    Message response = runFlow(getFlowName()).getMessage();
    assertThat((HttpResponseAttributes) response.getAttributes().getValue(), hasStatusCode(SC_OK));
  }

  protected boolean enableHttps() {
    return true;
  }
}
