/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.sdk.api.http.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;
import static org.mule.tck.probe.PollingProber.check;

import org.mule.runtime.core.api.util.func.CheckedRunnable;
import org.mule.sdk.api.http.HttpConstants.HttpStatus;
import org.mule.sdk.api.http.domain.entity.ByteArrayHttpEntity;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Rule;
import org.junit.Test;

public class HttpBackPressureTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  private AtomicBoolean stop;

  @Override
  protected String getConfigFile() {
    return "http-listener-backpressure-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    stop = new AtomicBoolean(false);
  }

  @Override
  protected void doTearDown() throws Exception {
    stop.set(true);
  }

  @Test
  public void backPressureWithFailStrategy() throws Exception {
    assertBackPressure("fail", SERVICE_UNAVAILABLE);
  }

  private void assertBackPressure(String path, HttpStatus expectedStatus) throws Exception {
    final String url = String.format("http://localhost:%s/%s", listenPort.getNumber(), path);

    Semaphore semaphore = new Semaphore(1024);

    final HttpRequest post = requestBuilder().uri(url).method("POST")
        .entity(new ByteArrayHttpEntity(path.getBytes()))
        .build();

    Thread thread = new Thread((CheckedRunnable) () -> {
      while (!stop.get()) {
        semaphore.acquire();
        httpClient.sendAsync(post, configurer -> configurer.setResponseTimeout(60_000).setFollowsRedirect(false))
            .whenComplete((response, e) -> {
              try {
                if (response != null && response.getStatusCode() == expectedStatus.getStatusCode()
                    && response.getReasonPhrase().equals(expectedStatus.getReasonPhrase())) {
                  stop.set(true);
                }
              } finally {
                semaphore.release();
              }
            });
      }
    });

    thread.start();
    check(30000, 100, stop::get);
    thread.join();
  }
}
