/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import io.qameta.allure.Story;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

@Story(HTTPS)
public class HttpListenerTlsRestrictedProtocolsAndCiphersTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort httpsPort = new DynamicPort("port");

  private static final String SERVER_CIPHER_SUITE_ENABLED = "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256";
  private static final String SERVER_CIPHER_SUITE_DISABLED = "TLS_DHE_RSA_WITH_AES_128_CBC_SHA";

  private static final String SERVER_PROTOCOL_ENABLED = "TLSv1.2";
  private static final String SERVER_PROTOCOL_DISABLED = "TLSv1";

  @Rule
  public SystemProperty serverKeyStoreFile =
      new SystemProperty("serverKeyStoreFile", getDefaultEnvironmentConfiguration().getTestServerKeyStore());

  @Rule
  public SystemProperty serverKeyStoreType =
      new SystemProperty("serverKeyStoreType", getDefaultEnvironmentConfiguration().getTestStoreType());

  @BeforeClass
  public static void createTlsPropertiesFile() throws Exception {
    PrintWriter writer = new PrintWriter(getTlsPropertiesFile(), "UTF-8");
    writer.println("enabledCipherSuites=" + SERVER_CIPHER_SUITE_ENABLED);
    writer.println("enabledProtocols=" + SERVER_PROTOCOL_ENABLED);
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
    return "http-listener-restricted-protocols-ciphers-config.xml";
  }

  @Test
  public void handshakeSuccessWhenUsingEnabledCipherSpec() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

    SSLSocket socket = createSocket(new String[] {SERVER_CIPHER_SUITE_ENABLED, SERVER_CIPHER_SUITE_DISABLED},
                                    new String[] {SERVER_PROTOCOL_ENABLED, SERVER_PROTOCOL_DISABLED});

    socket.addHandshakeCompletedListener(handshakeCompletedEvent -> latch.countDown());

    socket.startHandshake();

    assertTrue(latch.await(LOCK_TIMEOUT, TimeUnit.MILLISECONDS));
    assertEquals(SERVER_CIPHER_SUITE_ENABLED, socket.getSession().getCipherSuite());
    assertEquals(SERVER_PROTOCOL_ENABLED, socket.getSession().getProtocol());

    socket.close();
  }


  // In FIPS, the propagated exception depends on the security provided and its version. That is why we expect a generic
  // exception.
  @Test(expected = Exception.class)
  public void handshakeFailureWithDisabledCipherSuite() throws Exception {
    SSLSocket socket = createSocket(new String[] {SERVER_CIPHER_SUITE_DISABLED}, new String[] {SERVER_PROTOCOL_ENABLED});
    socket.startHandshake();
  }

  // In FIPS, the propagated exception depends on the security provided and its version. That is why we expect a generic
  // exception.
  @Test(expected = Exception.class)
  public void handshakeFailureWithDisabledProtocol() throws Exception {
    SSLSocket socket = createSocket(new String[] {SERVER_CIPHER_SUITE_ENABLED}, new String[] {SERVER_PROTOCOL_DISABLED});
    socket.startHandshake();
  }


  private SSLSocket createSocket(String[] cipherSuites, String[] enabledProtocols) throws Exception {
    TlsContextFactory tlsContextFactory = TlsContextFactory.builder()
        .trustStorePath(getDefaultEnvironmentConfiguration().getTestGenericTrustKeyStore())
        .trustStoreType(getDefaultEnvironmentConfiguration().getTestStoreType())
        .trustStorePassword("mulepassword")
        .build();

    SSLContext sslContext = tlsContextFactory.createSslContext();
    SSLSocketFactory socketFactory = sslContext.getSocketFactory();
    SSLSocket socket = (SSLSocket) socketFactory.createSocket("localhost", httpsPort.getNumber());

    socket.setEnabledCipherSuites(cipherSuites);
    socket.setEnabledProtocols(enabledProtocols);

    return socket;
  }

}
