/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.ntlm;

import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpHeaders.Names.AUTHORIZATION;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONNECTION;
import static org.mule.runtime.http.api.HttpHeaders.Names.WWW_AUTHENTICATE;
import static org.mule.runtime.http.api.HttpHeaders.Values.KEEP_ALIVE;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.NTLM;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.RunnerDelegateTo;

import java.util.Arrays;
import java.util.Collection;

import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;

@Story(NTLM)
@RunnerDelegateTo(Parameterized.class)
public class HttpStreamingRequestNtlmAuthTestCase extends AbstractNtlmTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("port");

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
    return Arrays.asList(new Object[][] {{"ntlmAuthRequestWithWorkstation", "Ursa-Minor", "LightCity"}});
  }

  @Override
  protected String getWorkstation() {
    return workstation;
  }

  @Override
  protected String getDomain() {
    return domain;
  }

  @Test
  @Description("Verifies a flow involving a NTLM Auth does not close the stream when keep alive (MULE-15581)")
  public void testStreamNotRemotelyClosed() throws Exception {
    Response response = Request.Post("http://localhost:" + port.getNumber() + "/test")
        .body(new StringEntity(TEST_MESSAGE, TEXT_PLAIN)).addHeader(CONNECTION, KEEP_ALIVE).execute();
    assertThat(response.returnResponse().getStatusLine().getStatusCode(), is(OK.getStatusCode()));
  }

  @Override
  protected String getConfigFile() {
    return "http-streaming-request-ntlm-auth-config.xml";
  }
}
