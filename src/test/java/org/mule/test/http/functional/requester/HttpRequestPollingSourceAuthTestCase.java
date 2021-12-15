/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.POLLING_SOURCE;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Test;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.FileUtils;

import java.io.IOException;

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
  @Issue("HTTPC-160")
  public void correctBasicAuth() throws InterruptedException {
    basicAuthLatch.await();
    assertThat(BasicAuthProcessor.response, is(DEFAULT_RESPONSE));
  }

  @Test
  @Description("Polling source should work correctly when digest auth is set correctly")
  @Issue("HTTPC-160")
  public void correctDigestAuth() throws InterruptedException {
    digestAuthLatch.await();
    assertThat(DigestAuthProcessor.response, is(DEFAULT_RESPONSE));
  }

  @Test
  @Description("Polling source should NOT work when basic auth is set incorrectly")
  @Issue("HTTPC-160")
  public void incorrectAuth() throws InterruptedException {
    sleep(5000);
    assertThat(BasicFailingAuth.requests, is(0));
  }

  @Override
  protected AbstractHandler createHandler(Server server) {
    AbstractHandler handler = super.createHandler(server);

    try {
      String realmPath = FileUtils.getResourcePath("auth/realm.properties", getClass());
      return HttpRequestAuthUtils.createAuthHandler(server, handler, realmPath, () -> {
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
}
