/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.auth;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static org.mule.runtime.core.api.util.FileUtils.getResourcePath;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;

import static org.junit.Assume.assumeFalse;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import io.qameta.allure.Issue;
import org.junit.Test;

@Issue("W-17282518")
// TODO W-17430471 Migrate 100 continue test to Munit
public class HttpRequestExpect100ContinueHeaderAuthTestCase extends AbstractHttpRequestTestCase {

  @Override
  protected String getConfigFile() {
    return "http-request-expect-header-authentication-config.xml";
  }

  @Test
  @Issue("W-17282518")
  public void validBasicAuthentication() throws Exception {
    assertValidRequest("basicAuthRequest", "user", "password");
  }

  @Test
  @Issue("W-17282518")
  public void validDigestAuth() throws Exception {
    assumeFalse("Digest authentication is based on MD5. So this should not run on FIPS",
                isFipsTesting());
    assertValidRequest("digestAuthRequest", "user", "password");
  }

  private void assertValidRequest(String flowName, String user, String password) throws Exception {
    CoreEvent event = flowRunner(flowName).withPayload(TEST_MESSAGE).withVariable("user", user)
        .withVariable("password", password).run();
    assertThat(event.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
  }
}
