/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import org.apache.http.HttpVersion;
import org.junit.Test;

public class HttpListenerResponseStreaming11TestCase extends HttpListenerResponseStreamingTestCase {

  @Override
  protected HttpVersion getHttpVersion() {
    return HttpVersion.HTTP_1_1;
  }

  // AUTO - String

  @Test
  public void string() throws Exception {
    final String url = getUrl("string");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithContentLengthHeader() throws Exception {
    final String url = getUrl("stringWithContentLengthHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithTransferEncodingHeader() throws Exception {
    final String url = getUrl("stringWithTransferEncodingHeader");
    testResponseIsChunkedEncoding(url, getHttpVersion());
  }

  @Test
  public void stringWithTransferEncodingAndContentLengthHeader() throws Exception {
    final String url = getUrl("stringWithTransferEncodingAndContentLengthHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  // AUTO - InputStream

  @Test
  public void inputStream() throws Exception {
    final String url = getUrl("inputStream");
    testResponseIsChunkedEncoding(url, getHttpVersion());
    streamIsClosed();
  }

  @Test
  public void inputStreamWithContentLengthHeader() throws Exception {
    final String url = getUrl("inputStreamWithContentLengthHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
    streamIsClosed();
  }

  @Test
  public void inputStreamWithTransferEncodingHeader() throws Exception {
    final String url = getUrl("inputStreamWithTransferEncodingHeader");
    testResponseIsChunkedEncoding(url, getHttpVersion());
    streamIsClosed();
  }

  @Test
  public void inputStreamWithTransferEncodingAndContentLengthHeader() throws Exception {
    final String url = getUrl("inputStreamWithTransferEncodingAndContentLengthHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
    streamIsClosed();
  }

  // NEVER - String

  @Test
  public void neverString() throws Exception {
    final String url = getUrl("neverString");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  @Test
  public void neverStringTransferEncodingHeader() throws Exception {
    final String url = getUrl("neverStringTransferEncodingHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
  }

  // NEVER - InputStream

  @Test
  public void neverInputStream() throws Exception {
    final String url = getUrl("neverInputStream");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
    streamIsClosed();
  }

  @Test
  public void neverInputStreamTransferEncodingHeader() throws Exception {
    final String url = getUrl("neverInputStreamTransferEncodingHeader");
    testResponseIsContentLengthEncoding(url, getHttpVersion());
    streamIsClosed();
  }

  // ALWAYS - String

  @Test
  public void alwaysString() throws Exception {
    final String url = getUrl("alwaysString");
    testResponseIsChunkedEncoding(url, getHttpVersion());
  }

  @Test
  public void alwaysStringContentLengthHeader() throws Exception {
    final String url = getUrl("alwaysStringContentLengthHeader");
    testResponseIsChunkedEncoding(url, getHttpVersion());
  }

  // ALWAYS - InputStream

  @Test
  public void alwaysInputStream() throws Exception {
    final String url = getUrl("alwaysInputStream");
    testResponseIsChunkedEncoding(url, getHttpVersion());
    streamIsClosed();
  }

  @Test
  public void alwaysInputStreamContentLengthHeader() throws Exception {
    final String url = getUrl("alwaysInputStreamContentLengthHeader");
    testResponseIsChunkedEncoding(url, getHttpVersion());
    streamIsClosed();
  }

  // Map

  @Test
  public void map() throws Exception {
    final String url = getUrl("map");
    testResponseIsContentLengthEncoding(url, getHttpVersion(), mapPayload.getValue());
  }

  @Test
  public void alwaysMap() throws Exception {
    final String url = getUrl("alwaysMap");
    testResponseIsChunkedEncoding(url, getHttpVersion(), mapPayload.getValue());
  }

  @Test
  public void neverMap() throws Exception {
    final String url = getUrl("neverMap");
    testResponseIsContentLengthEncoding(url, getHttpVersion(), mapPayload.getValue());
  }

}
