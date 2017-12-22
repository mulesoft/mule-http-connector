/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.runtime.http.api.HttpConstants.HttpStatus.DROPPED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;
import static org.mule.tck.probe.PollingProber.check;
import org.mule.runtime.core.api.util.func.CheckedRunnable;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class HttpBackPressureTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  @Inject
  private HttpService httpService;

  private AtomicBoolean stop;
  private HttpClient client;

  @Override
  protected String getConfigFile() {
    return "http-listener-backpressure-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    stop = new AtomicBoolean(false);
    client = httpService.getClientFactory().create(new HttpClientConfiguration.Builder()
        .setName("backPressure-test").build());
    client.start();
  }

  @Override
  protected void doTearDown() throws Exception {
    stop.set(true);
    if (client != null) {
      client.stop();
    }
  }

  @Test
  public void backPressureWithFailStrategy() throws Exception {
    assertBackPressure("fail", SERVICE_UNAVAILABLE);
  }

  @Test
  @Ignore("MULE-14284 - Runtime should notify if FAIL or DROP")
  public void backPressureWithDropStrategy() throws Exception {
    assertBackPressure("drop", DROPPED);
  }

  private void assertBackPressure(String path, HttpStatus expectedStatus) throws Exception {
    final String url = String.format("http://localhost:%s/%s", listenPort.getNumber(), path);

    Semaphore semaphore = new Semaphore(1024);

    final HttpRequest post = HttpRequest.builder().uri(url).method("POST")
        .entity(new ByteArrayHttpEntity(path.getBytes()))
        .build();

    new Thread((CheckedRunnable) () -> {
      while (!stop.get()) {
        semaphore.acquire();
        client.sendAsync(post, 60000, false, null).whenComplete((response, e) -> {
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
    }).start();

    check(30000, 100, stop::get);
  }
}
