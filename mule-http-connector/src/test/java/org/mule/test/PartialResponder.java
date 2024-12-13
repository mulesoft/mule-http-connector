/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Test server that responds a partial response and closes the connection. It's intended to test the requester behavior
 * on that scenario.
 */
public class PartialResponder {

  private static AcceptorThread acceptorThread = null;

  public static void spawnServer(Integer port) {
    try {
      acceptorThread = new AcceptorThread(port);
      acceptorThread.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void stopServer() {
    try {
      acceptorThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static class AcceptorThread extends Thread {

    private final ServerSocket acceptorSocket;

    public AcceptorThread(Integer port) throws IOException {
      acceptorSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
      try {
        // Only one accept per test.
        Socket peer = acceptorSocket.accept();
        acceptorSocket.close();

        Thread readerThread = new DevNullReaderThread(peer);
        readerThread.start();
        OutputStream outputStream = peer.getOutputStream();
        outputStream.write(("HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/plain; charset=iso-8859-1\r\n" +
            "Transfer-encoding: chunked\r\n" +
            "\r\n" +
            "1\r\n" +
            "A\r\n").getBytes());
        outputStream.flush();
        outputStream.close();

        readerThread.join();
      } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class DevNullReaderThread extends Thread {

    private final InputStream peerIS;

    public DevNullReaderThread(Socket peer) throws IOException {
      this.peerIS = peer.getInputStream();
    }

    @Override
    public void run() {
      boolean eos = false;
      while (!eos) {
        try {
          if (peerIS.read() == -1) {
            eos = true;
          }
        } catch (IOException e) {
          System.out.println(e.getMessage());
          eos = true;
        }
      }
    }
  }
}
