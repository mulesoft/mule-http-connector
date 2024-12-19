/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test;

import java.io.IOException;
import java.io.InputStream;

public final class TestUtils {

  private TestUtils() {
    // empty constructor to avoid wrong instantiation.
  }

  /**
   * Reads a String from an {@link InputStream} until certain pattern is found, and returns that String.
   * 
   * @param inputStream the stream to read.
   * @param pattern     the pattern to match.
   * @return a String that contains the pattern, or the whole content if the end of stream is reached.
   * @throws IOException if an error occurs while reading.
   */
  public static String readUntilPattern(InputStream inputStream, String pattern) throws IOException {
    StringBuilder requestAsText = new StringBuilder();
    byte[] inBuf = new byte[1024];
    boolean patternFound = false;
    boolean endOfStream = false;
    while (!patternFound && !endOfStream) {
      int bytesNum = inputStream.read(inBuf);
      if (bytesNum == -1) {
        endOfStream = true;
      } else {
        requestAsText.append(new String(inBuf, 0, bytesNum));
        if (requestAsText.toString().contains(pattern)) {
          patternFound = true;
        }
      }
    }
    return requestAsText.toString();
  }
}
