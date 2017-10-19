/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.functional.api.component.FunctionalTestProcessor.getFromFlow;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import org.mule.functional.api.component.FunctionalTestProcessor;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.qameta.allure.Feature;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
public class HttpListenerOverloadTestCase extends AbstractHttpTestCase {

  // This must match the default pool size in ContainerThreadPoolsConfig
  private static final int MAX_CONNECTIONS = 256;

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  private CountDownLatch activeProcessorsStarted;
  private CountDownLatch activeProcessorsFinished;
  private Latch keepProcessorsActive = new Latch();
  private AtomicInteger numberOfRequest = new AtomicInteger();
  private Executor httpClientExecutor;
  private ConcurrentLinkedQueue<Throwable> accumulatedErrors = new ConcurrentLinkedQueue<>();

  @Override
  protected String getConfigFile() {
    return "http-listener-overload-config.xml";
  }

  @Before
  public void setup() {
    // Need to configure the maximum number of connections of HttpClient, because the default is less than
    // the default number of workers in the HTTP listener.
    PoolingHttpClientConnectionManager mgr = new PoolingHttpClientConnectionManager();
    mgr.setDefaultMaxPerRoute(MAX_CONNECTIONS);
    mgr.setMaxTotal(MAX_CONNECTIONS);
    httpClientExecutor = Executor.newInstance(HttpClientBuilder.create().setConnectionManager(mgr).build());
  }

  /**
   * Phase 1: Start N requests to the listener, which will use all workers from the pool to wait on a latch inside a message processor.
   * Phase 2: Start a few requests to the listener, checking that they return "server busy" status code without getting into the flow message processors.
   * Phase 3: Release the latch, allowing the N requests to continue and verifying they succeed.
   * Phase 4: Verify that all requests were processed and no errors were thrown.
   */
  @Test
  public void overloadScenario() throws Exception {
    final String url = format("http://localhost:%s/", listenPort.getNumber());

    try {
      sendRequestUntilNoMoreWorkers("testFlow", url, MAX_CONNECTIONS - 1);

      for (int i = 0; i < 5; i++) {
        final HttpResponse response = Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), is(SERVICE_UNAVAILABLE.getStatusCode()));
        assertThat(response.getStatusLine().getReasonPhrase(), is(SERVICE_UNAVAILABLE.getReasonPhrase()));
        assertThat(IOUtils.toString(response.getEntity().getContent()), is("Scheduler unavailable"));
      }

    } finally {
      keepProcessorsActive.release();
      if (!activeProcessorsFinished.await(15, TimeUnit.SECONDS)) {
        fail(format("%d message processor invocations didn't finish", activeProcessorsFinished.getCount()));
      }
    }

    if (accumulatedErrors.size() > 0) {
      fail(accumulatedErrors.stream().limit(10).collect(Collectors.toList()).toString());
    }
  }

  private void sendRequestUntilNoMoreWorkers(String flowName, String url, int maxThreadsActive) throws Exception {
    activeProcessorsStarted = new CountDownLatch(maxThreadsActive);
    activeProcessorsFinished = new CountDownLatch(maxThreadsActive);
    configureTestComponent(flowName, maxThreadsActive);

    for (int i = 0; i < maxThreadsActive; i++) {
      executeRequestInAnotherThread(url);
    }

    if (!activeProcessorsStarted.await(15, TimeUnit.SECONDS)) {
      fail(format("%d message processor invocations weren't started", activeProcessorsStarted.getCount()));
    }
  }

  private void executeRequestInAnotherThread(final String url) {
    new Thread(() -> {
      try {
        HttpResponse response = httpClientExecutor.execute(Request.Get(url).connectTimeout(RECEIVE_TIMEOUT)).returnResponse();
        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        assertThat(IOUtils.toString(response.getEntity().getContent()), is("the result"));
      } catch (Throwable e) {
        accumulatedErrors.add(e);
      } finally {
        activeProcessorsFinished.countDown();
      }
    }).start();
  }

  /**
   * Configures a test component in a specific flow to block until a specific number of concurrent requests are reached.
   */
  private void configureTestComponent(String flowName, final int maxThreadsActive) throws Exception {
    FunctionalTestProcessor testProc = getFromFlow(locator, flowName);
    testProc.setEventCallback((event, component, muleContext) -> {
      try {
        activeProcessorsStarted.countDown();
        numberOfRequest.incrementAndGet();
        if (numberOfRequest.get() <= maxThreadsActive) {
          keepProcessorsActive.await();
        }
      } catch (InterruptedException e) {
        accumulatedErrors.add(e);
      }
    });
  }
}
