/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.server;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;

import static javax.servlet.http.HttpServletResponse.SC_CONTINUE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import static org.slf4j.LoggerFactory.getLogger;

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
import org.slf4j.Logger;

/**
 * Embedded HTTP mock server for testing MuleSoft flows in MUnit.
 *
 * <p>
 * This server runs on Jetty and supports dynamic registration of request handlers mapped to specific paths. Incoming HTTP
 * requests are delegated to the configured {@link SourceCallback}, allowing Mule flows to simulate real HTTP interactions.
 * </p>
 */
public class HTTPMockServer {

  private final Server server;
  private final ConcurrentHashMap<String, SourceCallback<?, ?>> pathToCallback = new ConcurrentHashMap<>();
  private static final Logger LOGGER = getLogger(HTTPMockServer.class);

  public HTTPMockServer(int port) throws Exception {
    this.server = new Server(port);
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    context.addServlet(new ServletHolder(new MockServlet()), "/*");
    context.addServlet(new ServletHolder(new ExpectContinueServlet()), "/expect-continue");
    context.addServlet(new ServletHolder(new DelayExpectContinueServlet()), "/expect-continue-delay");

    server.setHandler(context);
    server.start();
    LOGGER.info("HTTP mock server started on port {}", port);
  }

  public void invalidate() {
    try {
      server.stop();
      LOGGER.info("HTTP mock server stopped successfully");
    } catch (Exception e) {
      LOGGER.error("Failed to stop the HTTP mock server", e);
    }
  }

  public StubRemover addHandlerFor(String path, SourceCallback<?, ?> callback) {
    pathToCallback.put(path, callback);
    LOGGER.info("Registered handler for path: {}", path);
    return new StubRemover(path);
  }

  public class StubRemover {

    private final String path;

    public StubRemover(String path) {
      this.path = path;
    }

    public void removeStub() {
      pathToCallback.remove(path);
      LOGGER.info("Removed handler for path: {}", path);
    }
  }

  /**
   * Servlet responsible for handling standard HTTP requests (e.g., GET, POST) for the mock server. It delegates incoming requests
   * to the appropriate {@link SourceCallback} registered for the request path.
   *
   * <p>
   * This servlet reads the request body, transforms it using a custom {@link DelegateToFlowTransformer}, and writes back the
   * generated {@link HTTPMockServerResponse}, including headers, status code, and body.
   * </p>
   *
   * Supported HTTP methods:
   * <ul>
   * <li>GET</li>
   * <li>POST</li>
   * </ul>
   *
   * Response headers include:
   * <ul>
   * <li>Content-Type: application/json</li>
   * <li>Custom headers from the {@code HTTPMockServerResponse}</li>
   * </ul>
   *
   * Response status:
   * <ul>
   * <li>200+ for successful flow responses</li>
   * <li>404 if no handler is registered for the requested path</li>
   * </ul>
   */
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
        resp.setStatus(SC_NOT_FOUND);
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


  /**
   * Servlet dedicated to handling HTTP requests with the {@code Expect: 100-continue} header.
   *
   * <p>
   * This servlet simulates proper handling of the HTTP/1.1 "100 Continue" mechanism: upon receiving an
   * {@code Expect: 100-continue} header, it responds with a {@code 100 Continue} interim status before reading the request body.
   * </p>
   *
   * <p>
   * This servlet reads the request body, transforms it using a custom {@link DelegateToFlowTransformer}, and writes back the
   * generated {@link HTTPMockServerResponse}, including headers, status code, and body.
   * </p>
   *
   * <p>
   * It also tracks and exposes key processing timestamps via custom response headers:
   * </p>
   * <ul>
   * <li>{@code X-Expect-Header-Time} ? when the Expect header was received</li>
   * <li>{@code X-Continue-Sent-Time} ? when the 100 Continue response was sent</li>
   * <li>{@code X-Body-Received-Time} ? when the full request body was received</li>
   * </ul>
   *
   * Response status:
   * <ul>
   * <li>100 Continue (if applicable)</li>
   * <li>Final status from the {@code HTTPMockServerResponse}</li>
   * <li>404 if no handler is registered for the requested path</li>
   * </ul>
   *
   * Response headers include:
   * <ul>
   * <li>Standard response headers from {@code HTTPMockServerResponse}</li>
   * <li>Timestamp headers for diagnostics and verification</li>
   * </ul>
   *
   * Used for testing HTTP clients' behavior with the Expect/Continue mechanism in MUnit scenarios.
   */
  private class ExpectContinueServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String path = req.getRequestURI();
      SourceCallback<InputStream, HTTPMockRequestAttributes> callback =
        (SourceCallback<InputStream, HTTPMockRequestAttributes>) pathToCallback.get(path);

      long expectHeaderReceivedAtTimestamp = 0;
      long continueResponseSentAtTimestamp = 0;
      long requestBodyReceivedAtTimestamp;

      if (callback == null) {
        resp.setStatus(SC_NOT_FOUND);
        resp.getWriter().write("Not Found");
        return;
      }

      String expectHeader = req.getHeader("Expect");
      if (expectHeader != null && expectHeader.equalsIgnoreCase("100-continue")) {
        expectHeaderReceivedAtTimestamp = currentTimeMillis();
        resp.setStatus(SC_CONTINUE);
        resp.flushBuffer();
        continueResponseSentAtTimestamp = currentTimeMillis();
      }

      byte[] requestBody = toByteArray(req.getInputStream());
      requestBodyReceivedAtTimestamp = currentTimeMillis();
      HTTPMockServerResponse mockResponse = DelegateToFlowTransformer.delegate(callback, requestBody);

      byte[] responseBody = mockResponse.getBody() != null ? toByteArray(mockResponse.getBody().getValue()) : new byte[0];

      resp.setStatus(mockResponse.getStatusCode());
      resp.setContentLength(responseBody.length);
      resp.setCharacterEncoding("UTF-8");
      resp.setContentType("application/json");

      resp.addHeader("X-Expect-Header-Time", valueOf(expectHeaderReceivedAtTimestamp));
      resp.addHeader("X-Continue-Sent-Time", valueOf(continueResponseSentAtTimestamp));
      resp.addHeader("X-Body-Received-Time", valueOf(requestBodyReceivedAtTimestamp));

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

  /**
   * Servlet dedicated to handling HTTP requests with the {@code Expect: 100-continue} header, with an added artificial delay.
   *
   * <p>
   * This servlet simulates proper handling of the HTTP/1.1 "100 Continue" mechanism by first imposing a delay (e.g., 30 seconds)
   * before fully reading the request body and processing the request. This is used for testing scenarios where a timeout or
   * delayed response is needed.
   * </p>
   *
   * <p>
   * When a request is received at the mapping (e.g., "/expect-continue-delay"), the servlet:
   * </p>
   * <ol>
   * <li>Checks for the {@code Expect: 100-continue} header and, if present, sends an interim 100 Continue status.</li>
   * <li>Waits for a pre-configured delay period of 30,000 milli seconds.</li>
   * <li>Reads the full request body and delegates processing to the custom {@link DelegateToFlowTransformer}.</li>
   * <li>Generates an {@link HTTPMockServerResponse} with the final status, headers, and body.</li>
   * <li>Tracks key processing timestamps which are exposed via custom response headers:
   * <ul>
   * <li>{@code X-Expect-Header-Time}: Time when the Expect header was received.</li>
   * <li>{@code X-Continue-Sent-Time}: Time when the 100 Continue response was sent.</li>
   * <li>{@code X-Body-Received-Time}: Time when the full request body was received.</li>
   * </ul>
   * </li>
   * </ol>
   *
   * <p>
   * Response status:
   * </p>
   * <ul>
   * <li>100 Continue (if applicable)</li>
   * <li>Final status from the {@code HTTPMockServerResponse}</li>
   * <li>404 if no handler is registered for the requested path</li>
   * </ul>
   *
   * <p>
   * Response headers include:
   * </p>
   * <ul>
   * <li>Standard response headers from the {@code HTTPMockServerResponse}</li>
   * <li>Timestamp headers for diagnostics and verification</li>
   * </ul>
   *
   * <p>
   * This servlet is used for testing HTTP client behavior with the Expect/Continue mechanism under delayed response conditions in
   * MUnit scenarios.
   * </p>
   */
  private class DelayExpectContinueServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      long delayMillis = 30000;
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      String path = req.getRequestURI();
      SourceCallback<InputStream, HTTPMockRequestAttributes> callback =
        (SourceCallback<InputStream, HTTPMockRequestAttributes>) pathToCallback.get(path);

      if (callback == null) {
        resp.setStatus(SC_NOT_FOUND);
        resp.getWriter().write("Not Found");
        return;
      }

      byte[] requestBody = toByteArray(req.getInputStream());
      HTTPMockServerResponse mockResponse = DelegateToFlowTransformer.delegate(callback, requestBody);

      byte[] responseBody = mockResponse.getBody() != null
        ? toByteArray(mockResponse.getBody().getValue())
        : new byte[0];

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

}
