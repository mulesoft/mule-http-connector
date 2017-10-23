/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.functional.api.component.FunctionalTestProcessor.getFromFlow;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;

import org.mule.functional.api.component.FunctionalTestProcessor;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.qameta.allure.Story;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@Story("Worker overload handling")
public class HttpListenerOverloadTestCase extends AbstractHttpTestCase {

  // This must be at least the default pool size in ContainerThreadPoolsConfig
  private static final int MAX_CONNECTIONS = 512;

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  private Executor httpClientExecutor;
  private Latch keepProcessorsActive;
  private AtomicInteger numProcessedRequests;
  private AtomicInteger numServiceBusy;
  private ConcurrentLinkedQueue<Throwable> accumulatedErrors;
  private Semaphore waitForNextRequester;

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

    keepProcessorsActive = new Latch();
    numProcessedRequests = new AtomicInteger(0);
    numServiceBusy = new AtomicInteger(0);
    accumulatedErrors = new ConcurrentLinkedQueue<>();
    waitForNextRequester = new Semaphore(0);
  }

  /**
   * Phase 1: Start arbitrary requests to the listener, which will use all workers from the pool to wait on a latch inside a message processor.
   * Phase 2: When we get at least 5 "server busy" responses (which didn't enter the flow), we stop creating requests.
   * Phase 3: Release the latch, allowing the suspended flow executions to continue and verify they succeed.
   * Phase 4: Verify that all requests were processed and no errors were thrown.
   */
  @Test
  public void overloadScenario() throws Exception {
    final String url = format("http://localhost:%s/", listenPort.getNumber());
    List<Thread> tasks = new ArrayList<>();

    configureTestComponent("testFlow");

    while (numServiceBusy.get() < 5) {
      tasks.add(executeRequestInAnotherThread(url));
      waitForNextRequester.acquire();
    }

    keepProcessorsActive.release();

    for (Thread t : tasks) {
      t.join();
    }

    if (accumulatedErrors.size() > 0) {
      fail(accumulatedErrors.stream().limit(10).collect(Collectors.toList()).toString());
    }

    assertThat(numProcessedRequests.get(), greaterThan(tasks.size() / 2));
  }

  private Thread executeRequestInAnotherThread(final String url) {
    Thread task = new Thread(() -> {
      try {
        HttpResponse response = httpClientExecutor
            .execute(Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).socketTimeout(RECEIVE_TIMEOUT)).returnResponse();
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == SERVICE_UNAVAILABLE.getStatusCode()) {
          assertThat(response.getStatusLine().getReasonPhrase(), is(SERVICE_UNAVAILABLE.getReasonPhrase()));
          assertThat(IOUtils.toString(response.getEntity().getContent()), is("Scheduler unavailable"));

          numServiceBusy.incrementAndGet();
          waitForNextRequester.release();
        } else if (statusCode == OK.getStatusCode()) {
          assertThat(IOUtils.toString(response.getEntity().getContent()), is("the result"));
        } else {
          accumulatedErrors.add(new AssertionError("request returned invalid status code: " + statusCode));
        }
      } catch (SocketException e) {
        // ignore possible "connection refused" due to temporary selector shortage
      } catch (Throwable e) {
        accumulatedErrors.add(e);
      }
    });

    task.start();

    return task;
  }

  /**
   * Configures a test component in a specific flow to block until a specific number of concurrent requests are reached.
   */
  private void configureTestComponent(String flowName) throws Exception {
    FunctionalTestProcessor testProc = getFromFlow(locator, flowName);
    testProc.setEventCallback((event, component, muleContext) -> {
      try {
        numProcessedRequests.incrementAndGet();
        waitForNextRequester.release();
        keepProcessorsActive.await();
      } catch (Throwable e) {
        accumulatedErrors.add(e);
      }
    });
  }
}
