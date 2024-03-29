/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasAttributes;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.STREAMING;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpResponse;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import io.qameta.allure.Story;

@Story(STREAMING)
public class HttpStreamingTestCase extends AbstractHttpTestCase {

  protected static AtomicBoolean stop;

  @Rule
  public DynamicPort httpPort = new DynamicPort("httpPort");

  @Override
  protected String getConfigFile() {
    return "http-streaming-config.xml";
  }

  @Before
  public void setUp() {
    stop = new AtomicBoolean(false);
  }

  @Test
  public void requesterStreams() throws Exception {
    CoreEvent response = flowRunner("client").run();
    stop.set(true);
    assertThat(response.getMessage(), hasAttributes(instanceOf(HttpResponseAttributes.class)));
    assertThat(response.getMessage().getPayload().getValue(), instanceOf(CursorStreamProvider.class));
  }

  @Test
  public void listenerStreams() throws Exception {
    final String url = String.format("http://localhost:%s/", httpPort.getNumber());
    CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
    try {
      httpclient.start();
      Future<HttpResponse> future = httpclient.execute(HttpAsyncMethods.createGet(url), new BasicAsyncResponseConsumer(), null);
      stop.set(true);
      HttpResponse response = future.get();
      assertThat(response.getFirstHeader(TRANSFER_ENCODING).getValue(), containsString(CHUNKED));
    } finally {
      httpclient.close();
    }
  }

  protected static class StoppableInputStreamProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      InputStream inputStream = new InputStream() {

        @Override
        public int read() throws IOException {
          if (stop.get()) {
            return -1;
          } else {
            return 1;
          }
        }
      };
      return CoreEvent.builder(event).message(of(inputStream)).build();
    }

  }

}
