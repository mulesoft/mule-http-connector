/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.mule.runtime.api.exception.ExceptionHelper.getRootException;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

import org.mule.runtime.core.api.util.FileUtils;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import io.qameta.allure.Story;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Story(HTTPS)
public class HttpRequesterTlsRestrictedProtocolsAndCiphersTestCase extends AbstractHttpTestCase {

  private static final String CLIENT_CIPHER_SUITE_ENABLED = "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256";
  private static final String CLIENT_CIPHER_SUITE_DISABLED = "TLS_DHE_RSA_WITH_AES_128_CBC_SHA";

  private static final String CLIENT_PROTOCOL_ENABLED = "TLSv1.2";
  private static final String CLIENT_PROTOCOL_DISABLED = "TLSv1";

  @Rule
  public SystemProperty serverKeyStoreType =
      new SystemProperty("storeType", getDefaultEnvironmentConfiguration().getTestStoreType());

  @Rule
  public SystemProperty trustStoreFile =
      new SystemProperty("trustStoreFile", getDefaultEnvironmentConfiguration().getTestGenericTrustKeyStore());

  @Rule
  public SystemProperty clientKeystoreFile =
      new SystemProperty("clientKeyStoreFile", getDefaultEnvironmentConfiguration().getTestClientKeyStore());

  @Rule
  public DynamicPort httpsPort = new DynamicPort("httpsPort");

  private String body;

  @BeforeClass
  public static void createTlsPropertiesFile() throws Exception {
    PrintWriter writer = new PrintWriter(getTlsPropertiesFile(), "UTF-8");
    writer.println("enabledCipherSuites=" + CLIENT_CIPHER_SUITE_ENABLED);
    writer.println("enabledProtocols=" + CLIENT_PROTOCOL_ENABLED);
    writer.close();
  }

  @AfterClass
  public static void removeTlsPropertiesFile() {
    getTlsPropertiesFile().delete();
  }


  private static File getTlsPropertiesFile() {
    return new File("tls-default.conf");
  }

  @Override
  protected String getConfigFile() {
    return "http-request-restricted-protocols-ciphers-config.xml";
  }

  @Test
  public void handshakeSuccessWhenUsingEnabledCipherSpecAndProtocol() throws Exception {
    sendRequest(CLIENT_CIPHER_SUITE_DISABLED, CLIENT_PROTOCOL_DISABLED);
    assertThat(body, equalTo(TEST_MESSAGE));
  }

  @Test
  public void handshakeFailsWithDisabledCipherSuite() throws Exception {
    assertFailure(CLIENT_CIPHER_SUITE_ENABLED, CLIENT_PROTOCOL_DISABLED);
  }

  @Test
  public void handshakeFailsWithDisabledProtocol() throws Exception {
    assertFailure(CLIENT_CIPHER_SUITE_DISABLED, CLIENT_PROTOCOL_ENABLED);
  }

  private void assertFailure(String serverCipherSuiteDisabled, String serverProtocolDisabled) throws Exception {
    try {
      sendRequest(serverCipherSuiteDisabled, serverProtocolDisabled);
      fail();
    } catch (Exception e) {
      assertThat(getRootException(e), instanceOf(IOException.class));
    }
  }

  /**
   * Sends a request to an HTTP server with TLS that doesn't support a specific cipher suite and protocol.
   */
  private void sendRequest(String serverCipherSuiteDisabled, String serverProtocolDisabled) throws Exception {
    Server server = createTlsServer(serverCipherSuiteDisabled, serverProtocolDisabled);

    server.start();

    try {
      flowRunner("requestFlow").withPayload(TEST_MESSAGE).run();
    } finally {
      server.stop();
    }
  }

  /**
   * Creates a TLS server that doesn't support a specific cipher suite and protocol.
   */
  private Server createTlsServer(String disabledCipherSuite, String disabledProtocol) throws Exception {
    SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
    sslContextFactory
        .setKeyStorePath(FileUtils.getResourcePath(getDefaultEnvironmentConfiguration().getTestServerKeyStore(), getClass()));
    sslContextFactory.setKeyStoreType(getDefaultEnvironmentConfiguration().getTestStoreType());
    sslContextFactory.setKeyStorePassword("mulepassword");
    sslContextFactory.setKeyManagerPassword("mulepassword");
    sslContextFactory.addExcludeCipherSuites(disabledCipherSuite);
    sslContextFactory.addExcludeProtocols(disabledProtocol);

    Server server = new Server();
    ServerConnector connector = new ServerConnector(server, sslContextFactory);
    connector.setPort(httpsPort.getNumber());

    server.addConnector(connector);

    server.setHandler(new AbstractHandler() {

      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException {
        body = IOUtils.toString(baseRequest.getInputStream());
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(TEST_MESSAGE);
        baseRequest.setHandled(true);
      }
    });

    return server;
  }

}
