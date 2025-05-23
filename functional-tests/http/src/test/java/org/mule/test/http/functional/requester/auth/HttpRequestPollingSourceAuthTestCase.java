/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.auth;

import static org.mule.runtime.core.api.util.FileUtils.getResourcePath;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.POLLING_SOURCE;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;
import static org.mule.test.http.functional.requester.auth.HttpRequestAuthUtils.createAuthHandler;

import static java.lang.Thread.sleep;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeFalse;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import java.io.IOException;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
@Story(POLLING_SOURCE)
public class HttpRequestPollingSourceAuthTestCase extends AbstractHttpRequestTestCase {

  private static Latch basicAuthLatch = new Latch();
  private static Latch digestAuthLatch = new Latch();

  @Override
  protected String getConfigFile() {
    return "http-polling-auth.xml";
  }

  @Test
  @Description("Polling source should work correctly when basic auth is set correctly")
  public void correctBasicAuth() throws Exception {
    startFlow("basicAuthRequest");
    basicAuthLatch.await();
    assertThat(BasicAuthProcessor.response, is(DEFAULT_RESPONSE));
  }

  @Test
  @Description("Polling source should work correctly when digest auth is set correctly")
  public void correctDigestAuth() throws Exception {
    assumeFalse("Digest authentication is based on MD5. So this should not run on FIPS",
                isFipsTesting());
    startFlow("digestAuthRequest");
    digestAuthLatch.await();
    assertThat(DigestAuthProcessor.response, is(DEFAULT_RESPONSE));
  }

  @Test
  @Description("Polling source should NOT work when basic auth is set incorrectly")
  public void incorrectAuth() throws Exception {
    startFlow("failingBasicAuthRequest");
    sleep(RECEIVE_TIMEOUT);
    assertThat(BasicFailingAuth.requests, is(0));
  }

  @Override
  protected AbstractHandler createHandler(Server server) {
    AbstractHandler handler = super.createHandler(server);

    try {
      String realmPath = getResourcePath("auth/realm.properties", getClass());
      return createAuthHandler(server, handler, realmPath, () -> {
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static class BasicAuthProcessor implements Processor {

    public static int requests = 0;
    public static String response = null;

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      requests++;
      if (requests == 3) {
        response = event.getMessage().getPayload().getValue().toString();
        basicAuthLatch.release();
      }
      return event;
    }
  }

  public static class DigestAuthProcessor implements Processor {

    public static int requests = 0;
    public static String response = null;

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      requests++;
      if (requests == 2) {
        response = event.getMessage().getPayload().getValue().toString();
        digestAuthLatch.release();
      }
      return event;
    }
  }

  public static class BasicFailingAuth implements Processor {

    public static int requests = 0;

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      requests += 1;
      return event;
    }
  }

  private void startFlow(String flowName) throws Exception {
    ((Startable) getFlowConstruct(flowName)).start();
  }
}
