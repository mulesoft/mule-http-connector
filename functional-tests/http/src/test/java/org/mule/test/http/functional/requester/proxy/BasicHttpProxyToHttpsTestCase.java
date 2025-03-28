/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.proxy;

import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;
import static org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers.hasStatusCode;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import java.io.IOException;

import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.junit.Rule;
import org.junit.Test;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BasicHttpProxyToHttpsTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public SystemProperty trustStore =
      new SystemProperty("tlsTrustStore", getDefaultEnvironmentConfiguration().getTestGenericTrustKeyStore());

  @Rule
  public SystemProperty tlsClientKeyStore =
      new SystemProperty("tlsClientKeyStore", getDefaultEnvironmentConfiguration().getTestClientKeyStore());

  @Rule
  public SystemProperty storeType = new SystemProperty("storeType", getDefaultEnvironmentConfiguration().getTestStoreType());

  private static final String AUTHORIZED = "Authorized";
  private static final String PASSWORD = "dXNlcjpwYXNzd29yZA==";

  private Server httpsServer;

  @Override
  protected AbstractHandler createHandler(Server server) {
    return new ConnectHandler() {

      @Override
      protected boolean handleAuthentication(HttpServletRequest request, HttpServletResponse response, String address) {

        String authorization = request.getHeader("Proxy-Authorization");
        return authorization != null && authorization.equals("Basic " + PASSWORD);
      }
    };
  }

  @Override
  protected boolean enableHttps() {
    return true;
  }

  @Override
  public void startServer() throws Exception {
    super.startServer();
    httpsServer.start();
  }

  @Override
  public void stopServer() throws Exception {
    if (httpsServer != null) {
      httpsServer.stop();
    }
    super.stopServer();
  }

  @Override
  protected void enableHttpsServer(Server server) {
    httpsServer = new Server();
    httpsServer.setHandler(new AbstractHandler() {

      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(AUTHORIZED);

        baseRequest.setHandled(true);
      }
    });
    super.enableHttpsServer(httpsServer);
  }

  @Override
  protected String getConfigFile() {
    return "http-request-proxy-to-https-config.xml";
  }

  /**
   * Validates that during the CONNECT the HTTP proxy should pass the proxy authentication when accessing to an HTTPS.
   * https://github.com/AsyncHttpClient/async-http-client/issues/1152
   *
   * @throws Exception
   */
  @Test
  public void validProxyHttpConnectToHttpsAuth() throws Exception {
    Message response = runFlow("httpFlow").getMessage();

    assertThat((HttpResponseAttributes) response.getAttributes().getValue(), hasStatusCode(SC_OK));
    assertThat(response.getPayload().getValue(), equalTo(AUTHORIZED));
  }
}
