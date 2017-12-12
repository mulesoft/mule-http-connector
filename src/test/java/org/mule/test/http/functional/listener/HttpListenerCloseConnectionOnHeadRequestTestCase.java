/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import org.junit.Rule;
import org.junit.Test;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Probe;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.apache.http.HttpVersion.HTTP_1_1;


public class HttpListenerCloseConnectionOnHeadRequestTestCase extends AbstractHttpTestCase {

  private static final int POOLING_FREQUENCY_MILLIS = 1000;
  private static final int POOLING_TIMEOUT_MILLIS = 20000;
  private static final int BUFFER_SIZE = 200;
  private static final String PAYLOAD = "TEST";

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  @Override
  protected String getConfigFile() {
    return "http-listener-close-connection-on-head-request-config.xml";
  }

  @Test
  public void checkConnectionCloses() throws Exception {
    final Socket socket = new Socket("localhost", listenPort.getNumber());
    sendRequest(socket);
    final byte[] readBuffer = new byte[BUFFER_SIZE];
    new PollingProber(POOLING_TIMEOUT_MILLIS, POOLING_FREQUENCY_MILLIS).check(new Probe() {

      @Override
      public boolean isSatisfied() {
        try {
          return socket.getInputStream().read(readBuffer) == -1;
        } catch (IOException e) {
          return false;
        }
      }

      @Override
      public String describeFailure() {
        return "The connection from the server was not closed";
      }
    });
    socket.close();
  }

  private void sendRequest(Socket socket) throws IOException {
    PrintWriter writer = new PrintWriter(socket.getOutputStream());
    writer.println("HEAD / " + HTTP_1_1);
    writer.println("Host: www.example.com");
    writer.println("");
    writer.flush();
  }

  private static class PayloadSetterStreamingProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      return CoreEvent.builder(event)
          .message(Message.builder(event.getMessage()).value(new ByteArrayInputStream(PAYLOAD.getBytes())).build())
          .build();
    }
  }
}
