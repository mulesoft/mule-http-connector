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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Integer.max;
import static java.lang.Runtime.getRuntime;

public class PipedStreamGenerator {

  private static final List<PipedOutputStream> sources = new ArrayList<PipedOutputStream>();
  private static final Random random = new Random();

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
    return new String(array, Charset.forName("UTF-8"));
  }

  public static String writeChunkInStreams() throws IOException {
    String chunk = generateRandomString(8192);
    for (PipedOutputStream stream : sources) {
      writeChunkInStream(chunk, stream);
    }
    return "A chunk was written to " + sources.size() + " streams";
  }

  public static void closeStreams() throws IOException {
    for (PipedOutputStream stream : sources) {
      stream.close();
    }
    sources.clear();
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
