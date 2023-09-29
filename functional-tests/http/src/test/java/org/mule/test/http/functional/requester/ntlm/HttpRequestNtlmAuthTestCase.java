/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.ntlm;

import static org.mule.runtime.http.api.HttpHeaders.Names.AUTHORIZATION;
import static org.mule.runtime.http.api.HttpHeaders.Names.WWW_AUTHENTICATE;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import org.mule.test.runner.RunnerDelegateTo;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class HttpRequestNtlmAuthTestCase extends AbstractAuthNtlmTestCase {

  @Parameterized.Parameter(0)
  public String flowName;

  @Parameterized.Parameter(1)
  public String domain;

  @Parameterized.Parameter(2)
  public String workstation;

  @Before
  public void setup() {
    setupTestAuthorizer(AUTHORIZATION, WWW_AUTHENTICATE, SC_UNAUTHORIZED);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {{"ntlmAuthRequestWithDomain", "Ursa-Minor", null},
        {"ntlmAuthRequestWithoutDomain", "", null}, {"ntlmAuthRequestWithWorkstation", "Ursa-Minor", "LightCity"}});
  }

  @Override
  protected String getWorkstation() {
    return workstation;
  }

  @Override
  protected String getDomain() {
    return domain;
  }

  @Override
  protected String getFlowName() {
    return flowName;
  }

  @Override
  protected String getConfigFile() {
    return "http-request-ntlm-auth-config.xml";
  }


}
