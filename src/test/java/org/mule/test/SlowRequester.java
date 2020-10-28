/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test;

import static java.lang.Integer.max;
import static java.lang.Runtime.getRuntime;
import static java.lang.Thread.currentThread;
import static java.nio.charset.Charset.forName;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class SlowRequester {

  private static List<SlowRequesterThread> requestThreads = new ArrayList<>();

  public static void spawnSlowRequests(Integer port) {
    int selectorsCount = max(getRuntime().availableProcessors(), 2);
    CountDownLatch allRequestsStartedLatch = new CountDownLatch(selectorsCount);
    for (int i = 0; i < selectorsCount; ++i) {
      SlowRequesterThread requester = new SlowRequesterThread(port, allRequestsStartedLatch);
      requester.start();
      requestThreads.add(requester);
    }
    try {
      allRequestsStartedLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void closeSockets() {
    requestThreads.forEach(SlowRequesterThread::closeSocket);
    requestThreads.forEach(slowRequesterThread -> {
      try {
        slowRequesterThread.join();
      } catch (InterruptedException e) {
        currentThread().interrupt();
      }
    });
  }

  private static class SlowRequesterThread extends Thread {

    private Integer port;
    private Socket clientSocket;
    CountDownLatch allRequestsStartedLatch;

    SlowRequesterThread(Integer port, CountDownLatch allRequestsStartedLatch) {
      this.port = port;
      this.allRequestsStartedLatch = allRequestsStartedLatch;
    }

    @Override
    public void run() {
      try {
        String content = generateRandomString(1000);
        clientSocket = new Socket("localhost", port);
        String request = "GET /test HTTP/1.1\n" +
            "Host: localhost:" + port + "\n" +
            "Content-Length: " + content.length() + "\n";
        clientSocket.getOutputStream().write(request.getBytes());

        allRequestsStartedLatch.countDown();

        for (int i = 0; i < content.length(); ++i) {
          sleep(1000);
          clientSocket.getOutputStream().write(content.charAt(i));
        }
      } catch (IOException e) {
        // ignored
      } catch (InterruptedException e) {
        currentThread().interrupt();
      }
    }

    private String generateRandomString(int length) {
      byte[] array = new byte[length];
      new Random().nextBytes(array);
      return new String(array, forName("UTF-8"));
    }

    void closeSocket() {
      try {
        clientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
