/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Integer.max;
import static java.lang.Runtime.getRuntime;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PipedStreamGenerator {

  private static final List<PipedOutputStream> sources = new ArrayList<PipedOutputStream>();
  private static final Random random = new Random();
  private static final int DEFAULT_CHUNK_SIZE = 8192;

  public static PipedInputStream createPipedStream() throws IOException {
    PipedOutputStream source = new PipedOutputStream();
    PipedInputStream sink = new PipedInputStream(source);
    synchronized (sources) {
      sources.add(source);
      sources.notifyAll();
    }
    return sink;
  }

  private static void writeChunkInStream(String chunk, PipedOutputStream stream) throws IOException {
    stream.write(chunk.getBytes(), 0, chunk.length());
    stream.flush();
  }

  private static String generateRandomString(int len) {
    byte[] array = new byte[len];
    random.nextBytes(array);
    return new String(array, UTF_8);
  }

  public static void writeChunkInStreams() throws IOException, InterruptedException {
    // The default tolerance before delegating response streaming in other thread is 50 ms.
    sleep(50);
    String chunk = generateRandomString(DEFAULT_CHUNK_SIZE);
    synchronized (sources) {
      for (PipedOutputStream stream : sources) {
        writeChunkInStream(chunk, stream);
      }
    }
  }

  public static void closeStreams() throws IOException {
    synchronized (sources) {
      for (PipedOutputStream stream : sources) {
        stream.close();
      }
      sources.clear();
    }
  }

  public static void waitForSources() throws InterruptedException {
    int numberOfSources = availableProcessors();
    synchronized (sources) {
      while (sources.size() < numberOfSources) {
        sources.wait();
      }
    }
  }

  public static int availableProcessors() {
    return max(getRuntime().availableProcessors(), 2);
  }
}
