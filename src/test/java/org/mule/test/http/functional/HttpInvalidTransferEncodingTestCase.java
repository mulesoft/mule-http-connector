/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Socket;

import static java.lang.Integer.toHexString;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.api.util.StringUtils.isEmpty;

@Story("Invalid Transfer Encoding")
@Feature("Reject Invalid Transfer Encoding")
@Issue("HTTPC-149")
// TODO HTTPC-151: Migrate this test to MTF.
public class HttpInvalidTransferEncodingTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Override
  protected String getConfigFile() {
    return "http-invalid-transfer-encoding-config.xml";
  }

  @Test
  public void chunkedTransferEncodingIsAllowed() throws IOException {
    String content = "Test content";
    try (Socket clientSocket = new Socket("localhost", port.getNumber())) {
      StringBuilder request = new StringBuilder(128);
      request.append("POST /test HTTP/1.1").append(lineSeparator());
      request.append("Host: localhost:").append(port.getNumber()).append(lineSeparator());
      request.append("Transfer-Encoding: chunked").append(lineSeparator());
      request.append(lineSeparator());
      request.append(toHexString(content.length())).append(lineSeparator());
      request.append(content).append(lineSeparator());
      request.append("0").append(lineSeparator()).append(lineSeparator());
      clientSocket.getOutputStream().write(request.toString().getBytes(UTF_8));

      String response = getResponse(clientSocket);
      assertThat(response, containsString("HTTP/1.1 200"));
    }
  }

  @Test
  public void chunkedCaseInsensitiveTransferEncodingIsAllowed() throws IOException {
    String content = "Test content";
    try (Socket clientSocket = new Socket("localhost", port.getNumber())) {
      StringBuilder request = new StringBuilder(128);
      request.append("POST /test HTTP/1.1").append(lineSeparator());
      request.append("Host: localhost:").append(port.getNumber()).append(lineSeparator());
      request.append("Transfer-Encoding: cHuNkEd").append(lineSeparator());
      request.append(lineSeparator());
      request.append(toHexString(content.length())).append(lineSeparator());
      request.append(content).append(lineSeparator());
      request.append("0").append(lineSeparator()).append(lineSeparator());
      clientSocket.getOutputStream().write(request.toString().getBytes(UTF_8));

      String response = getResponse(clientSocket);
      assertThat(response, containsString("HTTP/1.1 200"));
    }
  }

  @Test
  public void chunkedWithQuotesTransferEncodingIsForbidden() throws IOException {
    String content = "Test content";
    try (Socket clientSocket = new Socket("localhost", port.getNumber())) {
      StringBuilder request = new StringBuilder(128);
      request.append("POST /test HTTP/1.1").append(lineSeparator());
      request.append("Host: localhost:").append(port.getNumber()).append(lineSeparator());
      request.append("Transfer-Encoding: 'chunked'").append(lineSeparator());
      request.append(lineSeparator());
      request.append(toHexString(content.length())).append(lineSeparator());
      request.append(content).append(lineSeparator());
      request.append("0").append(lineSeparator()).append(lineSeparator());
      clientSocket.getOutputStream().write(request.toString().getBytes(UTF_8));

      String response = getResponse(clientSocket);
      assertThat(response, containsString("HTTP/1.1 400"));
    }
  }

  @Test
  public void bothContentLengthAndTransferEncoding() throws IOException {
    String content = "Test content";
    try (Socket clientSocket = new Socket("localhost", port.getNumber())) {
      StringBuilder request = new StringBuilder(128);
      request.append("POST /test HTTP/1.1").append(lineSeparator());
      request.append("Host: localhost:").append(port.getNumber()).append(lineSeparator());
      request.append("Content-Length: 2").append(lineSeparator());
      request.append("Transfer-Encoding: chunked").append(lineSeparator());
      request.append(lineSeparator());
      request.append(toHexString(content.length())).append(lineSeparator());
      request.append(content).append(lineSeparator());
      request.append("0").append(lineSeparator()).append(lineSeparator());
      clientSocket.getOutputStream().write(request.toString().getBytes(UTF_8));

      String response = getResponse(clientSocket);
      assertThat(response, containsString("HTTP/1.1 200"));
    }
  }

  @Test
  public void multipleValidTransferEncodingSpecifiedInSameHeader() throws IOException {
    String content = "Test content";
    try (Socket clientSocket = new Socket("localhost", port.getNumber())) {
      StringBuilder request = new StringBuilder(128);
      request.append("POST /test HTTP/1.1").append(lineSeparator());
      request.append("Host: localhost:").append(port.getNumber()).append(lineSeparator());
      request.append("Transfer-Encoding: chunked, deflate").append(lineSeparator());
      request.append(lineSeparator());
      request.append(toHexString(content.length())).append(lineSeparator());
      request.append(content).append(lineSeparator());
      request.append("0").append(lineSeparator()).append(lineSeparator());
      clientSocket.getOutputStream().write(request.toString().getBytes(UTF_8));

      String response = getResponse(clientSocket);
      assertThat(response, containsString("HTTP/1.1 200"));
    }
  }

  @Test
  public void multipleValidTransferEncodingSpecifiedInMultipleHeaders() throws IOException {
    String content = "Test content";
    try (Socket clientSocket = new Socket("localhost", port.getNumber())) {
      StringBuilder request = new StringBuilder(128);
      request.append("POST /test HTTP/1.1").append(lineSeparator());
      request.append("Host: localhost:").append(port.getNumber()).append(lineSeparator());
      request.append("Transfer-Encoding: deflate").append(lineSeparator());
      request.append("Transfer-Encoding: chunked").append(lineSeparator());
      request.append(lineSeparator());
      request.append(toHexString(content.length())).append(lineSeparator());
      request.append(content).append(lineSeparator());
      request.append("0").append(lineSeparator()).append(lineSeparator());
      clientSocket.getOutputStream().write(request.toString().getBytes(UTF_8));

      String response = getResponse(clientSocket);
      assertThat(response, containsString("HTTP/1.1 200"));
    }
  }

  private static String getResponse(Socket socket) {
    try (StringWriter writer = new StringWriter()) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
      String line;
      while (!isEmpty(line = reader.readLine())) {
        writer.append(line).append("\r\n");
      }
      return writer.toString();
    } catch (IOException e) {
      return null;
    }
  }
}
