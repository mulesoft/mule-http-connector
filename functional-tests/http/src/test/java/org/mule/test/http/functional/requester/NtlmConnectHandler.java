/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;
import static org.glassfish.grizzly.http.server.Constants.CLOSE;
import static org.glassfish.grizzly.http.server.Constants.CONNECTION;
import static org.glassfish.grizzly.http.server.Constants.KEEPALIVE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.io.SelectorManager;
import org.eclipse.jetty.io.SocketChannelEndPoint;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.thread.Scheduler;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NtlmConnectHandler extends ConnectHandler {

  private final TestAuthorizer testAuthorizer;
  private SelectorManager selector;

  public NtlmConnectHandler(TestAuthorizer testAuthorizer) throws Exception {
    this.testAuthorizer = testAuthorizer;
  }

  @Override
  protected SelectorManager newSelectorManager() {
    selector = new Manager(getExecutor(), getScheduler(), 1);
    return selector;
  }

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (request.isSecure()) {
      // if the request is secure, the response
      // from the https target is being handled
      simpleResponseFromTarget(response);
    } else {
      super.handle(target, baseRequest, request, response);
    }
  }

  private void sendConnectResponse(HttpServletRequest request, HttpServletResponse response, int statusCode) {
    try {
      response.setStatus(statusCode);
      if (statusCode != SC_OK)
        response.setHeader(CONNECTION, KEEPALIVE);
      response.getOutputStream().close();
      LOG.debug("CONNECT response sent {} {}", request.getProtocol(), response.getStatus());
    } catch (IOException x) {
      // TODO: nothing we can do, close the connection
    }
  }

  protected void doHandleConnect(Request jettyRequest, HttpServletRequest request, HttpServletResponse response,
                                 String serverAddress) {
    jettyRequest.setHandled(true);
    try {
      boolean proceed = handleAuthentication(request, response, serverAddress);
      if (!proceed) {
        LOG.debug("Missing proxy authentication");
        sendConnectResponse(request, response, SC_PROXY_AUTHENTICATION_REQUIRED);
        return;
      }

      String host = serverAddress;
      int port = 80;
      int colon = serverAddress.indexOf(':');
      if (colon > 0) {
        host = serverAddress.substring(0, colon);
        port = Integer.parseInt(serverAddress.substring(colon + 1));
      }

      if (!validateDestination(host, port)) {
        LOG.debug("Destination {}:{} forbidden", host, port);
        sendConnectResponse(request, response, SC_FORBIDDEN);
        return;
      }

      SocketChannel channel = SocketChannel.open();
      channel.socket().setTcpNoDelay(true);
      channel.configureBlocking(false);
      InetSocketAddress address = new InetSocketAddress(host, port);
      channel.connect(address);

      AsyncContext asyncContext = request.startAsync();
      asyncContext.setTimeout(0);

      LOG.debug("Connecting to {}", address);
      ConnectContext connectContext =
          new ConnectContext(request, response, asyncContext, HttpConnection.getCurrentConnection().getEndPoint());
      selector.connect(channel, connectContext);
    } catch (Exception x) {
      onConnectFailure(request, response, null, x);
    }
  }


  @Override
  protected void handleConnect(org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response,
                               String serverAddress) {
    doHandleConnect(baseRequest, request, response, serverAddress);
  }

  @Override
  protected boolean handleAuthentication(HttpServletRequest request, HttpServletResponse response, String address) {
    try {
      return testAuthorizer.authorizeRequest(address, request, response, false);
    } catch (IOException e) {
      return false;
    }
  }

  private void simpleResponseFromTarget(HttpServletResponse response) throws IOException {
    response.setHeader(CONNECTION, CLOSE);
    response.setStatus(SC_OK);
    response.getOutputStream().flush();
    response.getOutputStream().close();
  }

  protected class Manager extends SelectorManager {

    private Manager(Executor executor, Scheduler scheduler, int selectors) {
      super(executor, scheduler, selectors);
    }

    @Override
    protected EndPoint newEndPoint(SelectableChannel selectableChannel, ManagedSelector managedSelector,
                                   SelectionKey selectionKey) {
      return new SocketChannelEndPoint((SocketChannel) selectableChannel, managedSelector, selectionKey, getScheduler());
    }

    @Override
    public Connection newConnection(SelectableChannel selectableChannel, EndPoint endpoint, Object attachment) {
      ConnectHandler.LOG.debug("Connected to {}", endpoint.getRemoteAddress());
      ConnectContext connectContext = (ConnectContext) attachment;
      UpstreamConnection connection = newUpstreamConnection(endpoint, connectContext);
      connection.setInputBufferSize(getBufferSize());
      return connection;
    }

    @Override
    protected void connectionFailed(SelectableChannel channel, Throwable ex, Object attachment) {
      ConnectContext connectContext = (ConnectContext) attachment;
      onConnectFailure(connectContext.getRequest(), connectContext.getResponse(), connectContext.getAsyncContext(), ex);
    }
  }
}
