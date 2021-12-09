/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.STREAMING;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Probe;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;

@Story(STREAMING)
public abstract class AbstractHttpRequestResponseStreamingTestCase extends AbstractHttpTestCase {

  private static final int POLL_TIMEOUT = 2000;
  private static final int POLL_DELAY = 200;

  @Rule
  public DynamicPort httpPort = new DynamicPort("httpPort");

  protected static Latch latch;
  private static AtomicBoolean executed;

  protected final PollingProber pollingProber = new PollingProber(POLL_TIMEOUT, POLL_DELAY);
  protected Probe processorExecuted = new Probe() {

    @Override
    public boolean isSatisfied() {
      return executed.get();
    }

    @Override
    public String describeFailure() {
      return "Processor should have executed at this point.";
    }

  };
  protected Probe processorNotExecuted = new Probe() {

    @Override
    public boolean isSatisfied() {
      return !executed.get();
    }

    @Override
    public String describeFailure() {
      return "Processor should not have executed at this point.";
    }

  };

  @Before
  public void setUp() {
    latch = new Latch();
    executed = new AtomicBoolean(false);
  }

  public static class StatusProcessor implements Processor {


    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      executed.set(true);
      return event;
    }

  }

  protected static class FillAndWaitInputStreamProcessor implements Processor {

    private static final int LISTENER_BUFFER = 8 * 1024;

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      InputStream inputStream = new InputStream() {

        private int sent = 0;

        @Override
        public int read() throws IOException {
          if (sent <= LISTENER_BUFFER) {
            sent++;
            return 1;
          } else {
            try {
              latch.await(RECEIVE_TIMEOUT, MILLISECONDS);
            } catch (InterruptedException e) {
              // Do nothing
            }
            return -1;
          }
        }
      };
      return CoreEvent.builder(event).message(of(inputStream)).build();
    }

  }
}
