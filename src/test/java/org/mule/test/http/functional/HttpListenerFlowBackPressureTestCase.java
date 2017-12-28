/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.functional.api.component.FunctionalTestProcessor.getFromFlow;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mule.functional.api.component.FunctionalTestProcessor;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.tck.junit4.rule.DynamicPort;

import com.ning.http.client.AsyncCompletionHandlerBase;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider;

import io.qameta.allure.Story;

@Story("Source overload handling")
public class HttpListenerFlowBackPressureTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  private Latch keepProcessorsActive;
  private AtomicInteger numProcessedRequests;
  private AtomicInteger okResponses;
  private AtomicInteger overloadResponses;
  private ConcurrentLinkedQueue<Throwable> accumulatedErrors;
  private CountDownLatch sentLatch;
  private CountDownLatch overloadResponseLatch;
  private CountDownLatch allResponseLatch;
  private CountDownLatch processedLatch;
  private AsyncHttpClient client;

  private static int BUFFER_SIZE = 256;
  private static int OVERLOAD_COUNT = 44;

  @Override
  protected String getConfigFile() {
    return "http-listener-flow-backpressure-config.xml";
  }

  @Before
  public void setup() {
    keepProcessorsActive = new Latch();
    numProcessedRequests = new AtomicInteger(0);
    okResponses = new AtomicInteger(0);
    overloadResponses = new AtomicInteger(0);
    accumulatedErrors = new ConcurrentLinkedQueue<>();
    sentLatch = new CountDownLatch(BUFFER_SIZE + OVERLOAD_COUNT);
    overloadResponseLatch = new CountDownLatch(OVERLOAD_COUNT);
    allResponseLatch = new CountDownLatch(BUFFER_SIZE + OVERLOAD_COUNT);
    processedLatch = new CountDownLatch(BUFFER_SIZE);
    client = new AsyncHttpClient(new GrizzlyAsyncHttpProvider(new AsyncHttpClientConfig.Builder().build()));

  }

  @After
  public void tearDown() {
    client.close();
  }

  /**
   * Phase 1: Start arbitrary requests to the listener, which will use all workers from the pool to wait on a latch inside a
   * message processor. Phase 2: When we get at least 5 "server busy" responses (which didn't enter the flow), we stop creating
   * requests. Phase 3: Release the latch, allowing the suspended flow executions to continue and verify they succeed. Phase 4:
   * Verify that all requests were processed and no errors were thrown.
   */
  @Test
  public void overloadScenario() throws Exception {
    final String url = format("http://localhost:%s/", listenPort.getNumber());

    configureTestComponent("testFlow");

    ExecutorService executorService = newFixedThreadPool(1);
    for (int i = 0; i < BUFFER_SIZE + OVERLOAD_COUNT; i++) {
      executorService.submit(() -> executeRequestInAnotherThread(url));
    }

    // Block until all requests are sent
    sentLatch.await();

    // Block until we get overload responses back, other response will be pending
    overloadResponseLatch.await();

    assertThat(overloadResponses.get(), equalTo(OVERLOAD_COUNT));
    assertThat(okResponses.get(), equalTo(0));

    // Now we've asserted back-pressure release latched processor and allow buffer to drain
    keepProcessorsActive.release();
    allResponseLatch.await();
    processedLatch.await();

    assertThat(overloadResponses.get(), equalTo(OVERLOAD_COUNT));
    assertThat(okResponses.get(), equalTo(BUFFER_SIZE));
    assertThat(numProcessedRequests.get(), equalTo(BUFFER_SIZE));
  }

  private void executeRequestInAnotherThread(final String url) {
    client.prepareGet(url).execute(new AsyncCompletionHandlerBase() {

      @Override
      public Response onCompleted(Response response) throws Exception {
        overloadResponseLatch.countDown();
        allResponseLatch.countDown();
        int statusCode = response.getStatusCode();
        if (statusCode == SERVICE_UNAVAILABLE.getStatusCode()) {
          overloadResponses.incrementAndGet();
          assertThat(response.getStatusText(), is(SERVICE_UNAVAILABLE.getReasonPhrase()));
          assertThat(response.getResponseBody(), is("Flow 'testFlow' is unable to accept new events at this time"));
        } else if (statusCode == OK.getStatusCode()) {
          okResponses.incrementAndGet();
          assertThat(response.getResponseBody(), is("the result"));
        } else {
          accumulatedErrors.add(new AssertionError("request returned invalid status code: " + statusCode));
        }
        return response;
      }
    });
    sentLatch.countDown();
  }

  /**
   * Configures a test component in a specific flow to block until a specific number of concurrent requests are reached.
   */
  private void configureTestComponent(String flowName) throws Exception {
    FunctionalTestProcessor testProc = getFromFlow(locator, flowName);
    testProc.setEventCallback((event, component, muleContext) -> {
      try {
        numProcessedRequests.incrementAndGet();
        keepProcessorsActive.await();
        processedLatch.countDown();
      } catch (Throwable e) {
        accumulatedErrors.add(e);
      }
    });
  }
}
