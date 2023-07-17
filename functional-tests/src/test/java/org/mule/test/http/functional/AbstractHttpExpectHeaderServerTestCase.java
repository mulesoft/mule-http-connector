/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.apache.commons.io.IOUtils.read;
import static java.lang.String.valueOf;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Rule;

/**
 * Abstract class for tests that require a mock HTTP server that handles the "Expect" header. Provides methods for starting mock
 * servers that accept or reject the incoming request.
 */
public abstract class AbstractHttpExpectHeaderServerTestCase extends AbstractHttpTestCase {

  private static final String CONTINUE_RESPONSE = "HTTP/1.1 100 Continue\r\n\r\n";
  private static final String EXPECTATION_FAILED_RESPONSE = "HTTP/1.1 417 Expectation Failed\r\n";

  @Rule
  public DynamicPort listenPort = new DynamicPort("httpPort");

  @Rule
  public SystemProperty persistentConnection;

  protected String requestBody;

  private AbstractMockServer server;

  protected AbstractHttpExpectHeaderServerTestCase() {}

  protected AbstractHttpExpectHeaderServerTestCase(boolean persistentConnection) {
    this.persistentConnection = new SystemProperty("persistentConnection", valueOf(persistentConnection));
  }

  protected void startExpectContinueServer() {
    startExpectContinueServer(false);
  }

  protected void startExpectContinueServer(boolean persistent) {
    server = new ExpectContinueMockServer(persistent);
    server.start();
  }

  protected void startExpectFailedServer() {
    startExpectFailedServer(false);
  }

  protected void startExpectFailedServer(boolean persistent) {
    server = new ExpectFailedMockServer(persistent);
    server.start();
  }

  protected void stopServer() {
    server.stop();
  }

  private abstract class AbstractMockServer implements Runnable {

    private Latch startedLatch = new Latch();
    private Latch finishedLatch = new Latch();
    private boolean persistent = false;

    public AbstractMockServer(boolean persistent) {
      this.persistent = persistent;
    }

    public void start() {
      try {
        Thread serverThread = new Thread(this);
        serverThread.start();
        startedLatch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException("Failed to start mock server", e);
      }
    }

    public void stop() {
      try {
        finishedLatch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException("Failed to stop mock server", e);
      }
    }

    @Override
    public void run() {
      try {
        ServerSocket serverSocket = new ServerSocket(listenPort.getNumber());
        startedLatch.release();
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 1);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream), 1);

        while (!reader.readLine().isEmpty()) {
          // Do nothing, consume headers until blank line.
        }

        process(reader, writer);

        reader.close();
        writer.close();
        if (!persistent) {
          socket.close();
          serverSocket.close();
        }
        finishedLatch.release();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    protected abstract void process(BufferedReader reader, BufferedWriter writer) throws IOException;
  }

  private class ExpectContinueMockServer extends AbstractMockServer {

    public ExpectContinueMockServer(boolean persistent) {
      super(persistent);
    }

    @Override
    protected void process(BufferedReader reader, BufferedWriter writer) throws IOException {
      writer.write(CONTINUE_RESPONSE);
      writer.flush();

      char[] body = new char[TEST_MESSAGE.length()];
      read(reader, body);
      requestBody = new String(body);

      String response = String.format("HTTP/1.1 200 OK\nContent-Length: %d\n\n%s", TEST_MESSAGE.length(),
                                      TEST_MESSAGE);

      writer.write(response);
      writer.flush();
    }
  }

  private class ExpectFailedMockServer extends AbstractMockServer {

    public ExpectFailedMockServer(boolean persistent) {
      super(persistent);
    }

    @Override
    protected void process(BufferedReader reader, BufferedWriter writer) throws IOException {
      writer.write(EXPECTATION_FAILED_RESPONSE);
      writer.flush();
    }
  }
}
