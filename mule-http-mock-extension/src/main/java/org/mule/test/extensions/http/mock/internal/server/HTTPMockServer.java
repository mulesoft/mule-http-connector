/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.server;

import org.mule.runtime.extension.api.runtime.source.SourceCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class HTTPMockServer {

  private final Server server;
  private final ConcurrentHashMap<String, SourceCallback<?, ?>> pathToCallback = new ConcurrentHashMap<>();

  public HTTPMockServer(int port) throws Exception {
    this.server = new Server(port);
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    context.addServlet(new ServletHolder(new MockServlet()), "/*");
    context.addServlet(new ServletHolder(new ExpectContinueServlet()), "/expect-continue");
    server.setHandler(context);
    server.start();
  }

  public void invalidate() {
    try {
      server.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public StubRemover addHandlerFor(String path, SourceCallback<?, ?> callback) {
    pathToCallback.put(path, callback);
    return new StubRemover(path);
  }

  public class StubRemover {

    private final String path;

    public StubRemover(String path) {
      this.path = path;
    }

    public void removeStub() {
      pathToCallback.remove(path);
    }
  }

  private class MockServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      handle(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String path = req.getRequestURI();
      SourceCallback<InputStream, HTTPMockRequestAttributes> callback =
          (SourceCallback<InputStream, HTTPMockRequestAttributes>) pathToCallback.get(path);

      if (callback == null) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.getWriter().write("Not Found");
        return;
      }

      byte[] requestBody = toByteArray(req.getInputStream());
      HTTPMockServerResponse mockResponse = DelegateToFlowTransformer.delegate(callback, requestBody);

      byte[] responseBody = mockResponse.getBody() != null ? toByteArray(mockResponse.getBody().getValue()) : new byte[0];

      resp.setStatus(mockResponse.getStatusCode());
      resp.setContentLength(responseBody.length);
      resp.setCharacterEncoding("UTF-8");
      resp.setContentType("application/json");

      mockResponse.getHeaders().entrySet().forEach(entry -> resp.addHeader(entry.getKey(), entry.getValue()));
      try (OutputStream os = resp.getOutputStream()) {
        os.write(responseBody);
        os.flush();
      }
    }

    private byte[] toByteArray(InputStream input) throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      int nRead;
      while ((nRead = input.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      return buffer.toByteArray();
    }
  }

  private class ExpectContinueServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String path = req.getRequestURI();
      SourceCallback<InputStream, HTTPMockRequestAttributes> callback =
          (SourceCallback<InputStream, HTTPMockRequestAttributes>) pathToCallback.get(path);

      long expectHeaderReceived = 0;
      long sent100Continue = 0;
      long bodyReceived;

      if (callback == null) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.getWriter().write("Not Found");
        return;
      }

      String expectHeader = req.getHeader("Expect");
      if (expectHeader != null && expectHeader.equalsIgnoreCase("100-continue")) {
        expectHeaderReceived = System.currentTimeMillis();
        resp.setStatus(HttpServletResponse.SC_CONTINUE);
        resp.flushBuffer();
        sent100Continue = System.currentTimeMillis();
      }

      byte[] requestBody = toByteArray(req.getInputStream());
      bodyReceived = System.currentTimeMillis();
      HTTPMockServerResponse mockResponse = DelegateToFlowTransformer.delegate(callback, requestBody);

      byte[] responseBody = mockResponse.getBody() != null ? toByteArray(mockResponse.getBody().getValue()) : new byte[0];

      resp.setStatus(mockResponse.getStatusCode());
      resp.setContentLength(responseBody.length);
      resp.setCharacterEncoding("UTF-8");
      resp.setContentType("application/json");

      resp.addHeader("X-Expect-Header-Time", String.valueOf(expectHeaderReceived));
      resp.addHeader("X-Continue-Sent-Time", String.valueOf(sent100Continue));
      resp.addHeader("X-Body-Received-Time", String.valueOf(bodyReceived));

      mockResponse.getHeaders().entrySet().forEach(entry -> resp.addHeader(entry.getKey(), entry.getValue()));
      try (OutputStream os = resp.getOutputStream()) {
        os.write(responseBody);
        os.flush();
      }
    }

    private byte[] toByteArray(InputStream input) throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      int nRead;
      while ((nRead = input.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      return buffer.toByteArray();
    }
  }
}
