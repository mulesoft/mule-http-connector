/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test;

import static org.mule.test.TestUtils.readUntilPattern;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles one request per accepted socket. It expects a GET with a query parameter named "sleep", which time-unit is
 * SECONDS. After receiving such request, it will start a chunked response, sending the first chunk immediately, and
 * starts waiting the specified number of seconds. After that timeout, it will send a second chunk and a zero-len third
 * chunk, finishing the response.
 * <p>
 * User can shortcut the timeout by calling forceFinishAllResponders().
 */
public class SlowResponderServer {

  private static final List<SlowResponderThread> responderThreads = new ArrayList<>();
  private static AcceptorThread acceptorThread;

  public static void startServer(int port) {
    acceptorThread = new AcceptorThread(port);
    acceptorThread.start();
  }

  public static void stopServer() {
    try {
      acceptorThread.closeAcceptor();
      for (SlowResponderThread thread : responderThreads) {
        thread.join();
      }
      acceptorThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void forceFinishAllResponders() {
    for (SlowResponderThread thread : responderThreads) {
      thread.wakeUpAndFinishResponse();
    }
  }

  private static class AcceptorThread extends Thread {

    private final AtomicBoolean stoppedFlag = new AtomicBoolean(false);
    private final int port;
    private ServerSocket acceptorSocket;

    public AcceptorThread(int port) {
      this.port = port;
    }

    @Override
    public void run() {
      try (ServerSocket acceptor = new ServerSocket(port)) {
        acceptorSocket = acceptor;
        while (!stoppedFlag.get()) {
          Socket accepted = acceptor.accept();
          SlowResponderThread responderThread = new SlowResponderThread(accepted);
          responderThread.start();
          responderThreads.add(responderThread);
        }
      } catch (IOException e) {
        // expected on close
      }
    }

    public void closeAcceptor() {
      try {
        stoppedFlag.set(true);
        acceptorSocket.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class SlowResponderThread extends Thread {

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean woke;

    public SlowResponderThread(Socket peerSocket) throws IOException {
      this.woke = false;
      this.inputStream = peerSocket.getInputStream();
      this.outputStream = peerSocket.getOutputStream();
    }

    @Override
    public void run() {
      try {
        String requestAsString = readUntilPattern(inputStream, "\r\n\r\n");
        int sleepMillis = getSleepTimeMillis(requestAsString);

        sendHeader();
        sendChunk("Test ");
        waitTimeoutOrSignal(sleepMillis);
        sendChunk("payload");
        sendLastChunk();
      } catch (IOException e) {
        throw new RuntimeException("Error on a thread of the SlowResponderServer", e);
      }
    }

    private synchronized void waitTimeoutOrSignal(int sleepMillis) {
      long startMillis = currentTimeMillis();
      long endMillis = startMillis + sleepMillis;
      while (!woke) {
        long nowMillis = currentTimeMillis();
        if (nowMillis >= endMillis) {
          break;
        }
        try {
          this.wait(endMillis - nowMillis);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }

    public synchronized void wakeUpAndFinishResponse() {
      woke = true;
      notifyAll();
    }

    private int getSleepTimeMillis(String requestAsString) {
      String queryParamName = "sleep=";
      String[] split = requestAsString.split(queryParamName);
      assert split.length == 2;
      String afterQueryParamName = split[1];
      String sleepTimeAsString = afterQueryParamName.split("[ &]")[0];
      return 1000 * Integer.parseInt(sleepTimeAsString);
    }

    private void sendChunk(String chunk) throws IOException {
      String hexLen = Integer.toHexString(chunk.length());
      String chunkWithLen = format("%s\r\n%s\r\n", hexLen, chunk);
      outputStream.write(chunkWithLen.getBytes());
    }

    private void sendLastChunk() throws IOException {
      sendChunk("");
    }

    private void sendHeader() throws IOException {
      outputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
      outputStream.write("Connection: close\r\n".getBytes());
      outputStream.write("Transfer-Encoding: chunked\r\n".getBytes());
      outputStream.write("\r\n".getBytes());
    }
  }
}
