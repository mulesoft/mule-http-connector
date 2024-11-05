/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.streaming;

import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONNECTION;
import static org.mule.runtime.http.api.HttpHeaders.Names.PROXY_AUTHENTICATE;
import static org.mule.runtime.http.api.HttpHeaders.Names.PROXY_AUTHORIZATION;
import static org.mule.runtime.http.api.HttpHeaders.Values.KEEP_ALIVE;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.NTLM;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.PROXY;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.STREAMING;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;

import static javax.servlet.http.HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;

import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeFalse;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.ntlm.AbstractAuthNtlmTestCase;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;


@Issue("W-16606364")
@Stories({@Story(PROXY), @Story(NTLM), @Story(STREAMING)})
public class HttpStreamingRequestNtlmProxyConfigTestCase
    extends AbstractAuthNtlmTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("port");

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
    return "http-streaming-request-ntlm-proxy-config.xml";
  }

  @Override
  @Description("Verifies a flow involving a NTLM Auth via proxy config does not close the stream when kept alive ")
  public void validNtlmAuth() throws Exception {
    // The server that is working as a "proxy" (and executing the authentication) here is the one we usually use as the "target",
    // but it works for the test purposes.
    Response response =
        Request.Post("http://localhost:" + port.getNumber() + "/test").body(new StringEntity(TEST_MESSAGE, TEXT_PLAIN))
            .addHeader(CONNECTION, KEEP_ALIVE).execute();
    assertThat(response.returnResponse().getStatusLine().getStatusCode(), is(OK.getStatusCode()));
  }
}
