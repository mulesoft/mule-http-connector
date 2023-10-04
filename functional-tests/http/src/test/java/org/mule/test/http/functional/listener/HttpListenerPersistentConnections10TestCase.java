/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpHeaders.Values.CLOSE;
import static org.mule.runtime.http.api.HttpHeaders.Values.KEEP_ALIVE;

import java.io.IOException;

import org.apache.http.HttpVersion;
import org.junit.Test;

public class HttpListenerPersistentConnections10TestCase extends HttpListenerPersistentConnectionsTestCase {

  @Override
  protected HttpVersion getHttpVersion() {
    return HttpVersion.HTTP_1_0;
  }

  @Test
  public void nonPersistentCheckHeader() throws Exception {
    assertThat(performRequest(nonPersistentPort.getNumber(), getHttpVersion(), false), is(CLOSE));
  }

  @Test
  public void persistentCheckHeader() throws Exception {
    // Since in 1.0 keep alive is not the default, it has to be explicit for
    // persistent connections
    assertThat(performRequest(persistentPort.getNumber(), getHttpVersion(), false), is(KEEP_ALIVE));
  }

  @Test
  public void persistentCloseHeaderCheckHeader() throws Exception {
    assertThat(performRequest(persistentPortCloseHeader.getNumber(), getHttpVersion(), false), is(CLOSE));
  }

  @Test
  public void persistentEchoCheckHeader() throws IOException {
    assertThat(performRequest(persistentStreamingPort.getNumber(), getHttpVersion(), true), is(CLOSE));
  }

  /**
   * <h1>MULE-8502</h1>
   * 
   * <a href="http://tools.ietf.org/html/rfc2068#section-19.7.1">rfc2068#section-19.7.1</a> states that a 1.1. server cannot send
   * chunked content to a 1.0 client. In this case, the only way for the server to indicate that the transmission of the content
   * has finished is to close the connection (and send the appropriate header indicating this) Although the "Transfer-encoding:
   * Chunked" header is sent in the response, the client should ignore it since it is not part of the 1.0 spec
   * 
   * @throws IOException
   */
  @Test
  public void persistentStreamingTransformerCheckHeader() throws IOException {
    assertThat(performRequest(persistentStreamingTransformerPort.getNumber(), getHttpVersion(), true), is(CLOSE));
  }

  @Test
  public void persistentConnectionStreamingTransformerClosing() throws Exception {
    assertConnectionClosesAfterSend(persistentStreamingTransformerPort, getHttpVersion());
  }

}
