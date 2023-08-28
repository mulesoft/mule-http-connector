/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.proxy;

import static javax.servlet.http.HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpHeaders.Names.PROXY_AUTHENTICATE;
import static org.mule.runtime.http.api.HttpHeaders.Names.PROXY_AUTHORIZATION;

import org.junit.Before;
import org.mule.test.http.functional.requester.AbstractAuthNtlmTestCase;

import io.qameta.allure.Description;

public class HttpRequestNtlmProxyTestCase extends AbstractAuthNtlmTestCase {

  @Before
  public void setup() {
    setupTestAuthorizer(PROXY_AUTHORIZATION, PROXY_AUTHENTICATE, SC_PROXY_AUTHENTICATION_REQUIRED);
  }

  @Override
  protected String getConfigFile() {
    return "http-request-ntlm-proxy-config.xml";
  }

  @Override
  @Description("Verifies that NTLM Auth is successfully performed.")
  public void validNtlmAuth() throws Exception {
    super.validNtlmAuth();
    assertThat(requestUrl, is("http://localhost:9999/basePath/requestPath"));
  }
}
