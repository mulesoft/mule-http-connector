/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.runtime.http.api.HttpHeaders.Names.CONNECTION;
import static org.mule.runtime.http.api.HttpHeaders.Values.KEEP_ALIVE;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.mule.runtime.core.api.util.StringUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;

public abstract class HttpListenerPersistentConnectionsTestCase extends AbstractHttpTestCase {

  private static final int HTTP_OK = 200;
  private static final int GET_TIMEOUT = 1000;

  @Rule
  public DynamicPort nonPersistentPort = new DynamicPort("nonPersistentPort");

  @Rule
  public DynamicPort persistentPort = new DynamicPort("persistentPort");
  @Rule
  public DynamicPort persistentPortCloseHeader = new DynamicPort("persistentPortCloseHeader");
  @Rule
  public DynamicPort persistentStreamingPort = new DynamicPort("persistentStreamingPort");
  @Rule
  public DynamicPort persistentStreamingTransformerPort = new DynamicPort("persistentStreamingTransformerPort");

  protected abstract HttpVersion getHttpVersion();

  @Override
  protected String getConfigFile() {
    return "http-listener-persistent-connections-config.xml";
  }

  protected void assertConnectionClosesAfterSend(DynamicPort port, HttpVersion httpVersion) throws IOException {
    Socket socket = new Socket("localhost", port.getNumber());
    sendRequest(socket, httpVersion);
    assertResponse(getResponse(socket), true);

    sendRequest(socket, httpVersion);
    assertResponse(getResponse(socket), false);

    socket.close();
  }

  protected void assertConnectionClosesAfterTimeout(DynamicPort port, HttpVersion httpVersion)
      throws IOException, InterruptedException {
    Socket socket = new Socket("localhost", port.getNumber());
    sendRequest(socket, httpVersion);
    assertResponse(getResponse(socket), true);

    sendRequest(socket, httpVersion);
    assertResponse(getResponse(socket), true);

    Thread.sleep(3000);

    sendRequest(socket, httpVersion);
    assertResponse(getResponse(socket), false);

    socket.close();
  }

  protected void assertConnectionClosesWithRequestConnectionCloseHeader(DynamicPort port, HttpVersion httpVersion)
      throws IOException, InterruptedException {
    Socket socket = new Socket("localhost", port.getNumber());
    sendRequest(socket, httpVersion);
    assertResponse(getResponse(socket), true);

    sendRequest(socket, httpVersion);
    assertResponse(getResponse(socket), true);

    PrintWriter writer = new PrintWriter(socket.getOutputStream());
    writer.println("GET / " + httpVersion);
    writer.println("Host: www.example.com");
    writer.println("Connection: close");
    writer.println("");
    writer.flush();
    assertResponse(getResponse(socket), true);

    sendRequest(socket, httpVersion);
    assertResponse(getResponse(socket), false);

    socket.close();
  }

  protected String performRequest(int port, HttpVersion httpVersion, boolean keepAlive) throws IOException {
    HttpResponse response = doPerformRequest(port, httpVersion, keepAlive);
    Header connectionHeader = response.getFirstHeader(CONNECTION);
    return connectionHeader != null ? connectionHeader.getValue() : null;
  }

  private HttpResponse doPerformRequest(int port, HttpVersion httpVersion, boolean keepAlive)
      throws IOException, ClientProtocolException {
    String url = String.format("http://localhost:%s/", port);
    Request request = Request.Get(url).version(httpVersion).connectTimeout(GET_TIMEOUT);
    if (keepAlive) {
      request = request.addHeader(CONNECTION, KEEP_ALIVE);
    }
    HttpResponse response = request.execute().returnResponse();
    assertThat(response.getStatusLine().getStatusCode(), is(HTTP_OK));
    return response;
  }

  private void assertResponse(String response, boolean shouldBeValid) {
    if (shouldBeValid) {
      assertThat(response, not(emptyOrNullString()));
    } else {
      assertThat(response, emptyOrNullString());
    }
  }

  private void sendRequest(Socket socket, HttpVersion httpVersion) throws IOException {
    PrintWriter writer = new PrintWriter(socket.getOutputStream());
    writer.println("GET / " + httpVersion);
    writer.println("Host: www.example.com");
    writer.println("");
    writer.flush();
  }

  private String getResponse(Socket socket) {
    try {
      StringWriter writer = new StringWriter();
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      if (reader != null) {
        String line;
        while (!StringUtils.isEmpty(line = reader.readLine())) {
          writer.append(line).append("\r\n");
        }
      }
      return writer.toString();
    } catch (IOException e) {
      return null;
    }
  }

}
