/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpHeaders.Values.CLOSE;

import java.io.IOException;

import org.apache.http.HttpVersion;
import org.junit.Test;

public class HttpListenerPersistentConnections11TestCase extends HttpListenerPersistentConnectionsTestCase {

  @Override
  protected HttpVersion getHttpVersion() {
    return HttpVersion.HTTP_1_1;
  }

  @Test
  public void nonPersistentCheckHeader() throws Exception {
    assertThat(performRequest(nonPersistentPort.getNumber(), getHttpVersion(), false), is(CLOSE));
  }

  @Test
  public void persistentCheckHeader() throws Exception {
    assertThat(performRequest(persistentPort.getNumber(), getHttpVersion(), false), is(nullValue()));
  }

  @Test
  public void persistentCloseHeaderCheckHeader() throws Exception {
    assertThat(performRequest(persistentPortCloseHeader.getNumber(), getHttpVersion(), false), is(CLOSE));
  }

  @Test
  public void persistentEchoCheckHeader() throws IOException {
    assertThat(performRequest(persistentStreamingPort.getNumber(), getHttpVersion(), true), is(nullValue()));
  }

  @Test
  public void persistentStreamingTransformerCheckHeader() throws IOException {
    assertThat(performRequest(persistentStreamingTransformerPort.getNumber(), getHttpVersion(), true), is(nullValue()));
  }

  @Test
  public void nonPersistentConnectionClosing() throws Exception {
    assertConnectionClosesAfterSend(nonPersistentPort, getHttpVersion());
  }

  @Test
  public void persistentConnectionClosing() throws Exception {
    assertConnectionClosesAfterTimeout(persistentPort, getHttpVersion());
  }

  @Test
  public void persistentConnectionClosingWithRequestConnectionCloseHeader() throws Exception {
    assertConnectionClosesWithRequestConnectionCloseHeader(persistentPort, getHttpVersion());
  }

  @Test
  public void persistentConnectionCloseHeaderClosing() throws Exception {
    assertConnectionClosesAfterSend(persistentPortCloseHeader, getHttpVersion());
  }

}
