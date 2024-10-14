/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test;

import static org.mule.test.TestUtils.readUntilPattern;

import static java.lang.Integer.max;
import static java.lang.Runtime.getRuntime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Test utility class that spawns threads that make a GET to the endpoint "/proxy" with the queryParam "sleep".
 * <p>
 * The number of threads corresponds to the number of listener selector threads (max(getRuntime().availableProcessors(), 2)).
 */
public class ParallelRequester {

  private static final List<RequestThread> threads = new ArrayList<>();

  public static void startRequests(int port, int sleepTimeSeconds) throws IOException {
    int selectorsCount = max(getRuntime().availableProcessors(), 2);
    for (int i = 0; i < selectorsCount; ++i) {
      RequestThread thread = new RequestThread(port, sleepTimeSeconds);
      thread.start();
      threads.add(thread);
    }
  }

  public static void waitRequests() throws InterruptedException {
    for (Thread t : threads) {
      t.join();
    }
  }

  private static class RequestThread extends Thread {

    private final Socket socket;
    private final int sleepTimeSeconds;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public RequestThread(int port, int sleepTimeSeconds) throws IOException {
      this.sleepTimeSeconds = sleepTimeSeconds;
      this.socket = new Socket("localhost", port);
      this.inputStream = socket.getInputStream();
      this.outputStream = socket.getOutputStream();
    }

    @Override
    public void run() {
      try {
        sendGet();
        readUntilPattern(inputStream, "0\r\n\r\n");
        socket.close();
      } catch (IOException e) {
        throw new RuntimeException("Error while executing one client of the ParallelRequester", e);
      }
    }

    private void sendGet() throws IOException {
      outputStream.write(("GET /proxy?sleep=" + sleepTimeSeconds + " HTTP/1.1\r\n").getBytes());
      outputStream.write("Host: localhost\r\n".getBytes());
      outputStream.write("\r\n".getBytes());
    }
  }
}
