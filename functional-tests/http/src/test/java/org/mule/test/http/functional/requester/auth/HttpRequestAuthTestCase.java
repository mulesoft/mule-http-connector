/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.auth;

import static org.mule.runtime.core.api.util.FileUtils.getResourcePath;
import static org.mule.test.http.functional.requester.auth.HttpRequestAuthUtils.createAuthHandler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Test;

public class HttpRequestAuthTestCase extends AbstractHttpRequestTestCase {

  private int requestCount = 0;

  @Override
  protected String getConfigFile() {
    return "http-request-auth-config.xml";
  }

  @Test
  public void validBasicNonPreemptiveAuthentication() throws Exception {
    assertValidRequest("basicAuthRequest", "user", "password", false);
    assertThat(requestCount, is(2));
  }

  @Test
  public void validBasicPreemptiveAuthentication() throws Exception {
    assertValidRequest("basicAuthRequest", "user", "password", true);
    assertThat(requestCount, is(1));
  }

  @Test
  public void validDigestAuth() throws Exception {
    assertValidRequest("digestAuthRequest", "user", "password", false);
  }

  private void assertValidRequest(String flowName, String user, String password, boolean preemptive) throws Exception {
    CoreEvent event = flowRunner(flowName).withPayload(TEST_MESSAGE).withVariable("user", user)
        .withVariable("password", password).withVariable("preemptive", preemptive).run();

    assertThat(event.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
  }

  @Override
  protected AbstractHandler createHandler(Server server) {
    AbstractHandler handler = super.createHandler(server);
    try {
      String realmPath = getResourcePath("auth/realm.properties", getClass());
      return createAuthHandler(server, handler, realmPath, () -> requestCount++);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
