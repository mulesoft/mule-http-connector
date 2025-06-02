/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.sdk.api.http.HttpConstants.Method.POST;
import static org.mule.tck.processor.FlowAssert.verify;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.certificate.CertificateData;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextFactoryBuilder;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.sdk.api.http.client.HttpClient;
import org.mule.sdk.api.http.domain.entity.ByteArrayHttpEntity;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.http.domain.message.response.HttpResponse;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HttpListenerValidateCertificateTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort portWithValidation = new DynamicPort("port1");

  @Rule
  public DynamicPort portWithoutValidation = new DynamicPort("port2");

  @Rule
  public SystemProperty tlsTrustStore =
      new SystemProperty("tlsTrustStore", getDefaultEnvironmentConfiguration().getTestSslCaCerts());

  @Rule
  public SystemProperty tlsServerKeyStore =
      new SystemProperty("tlsServerKeyStore", getDefaultEnvironmentConfiguration().getTestServerKeyStore());

  @Rule
  public SystemProperty password =
      new SystemProperty("password", getDefaultEnvironmentConfiguration().resolveStorePassword("changeit"));

  @Rule
  public SystemProperty storeType = new SystemProperty("storeType", getDefaultEnvironmentConfiguration().getTestStoreType());


  // Uses a new HttpClient because it is needed to configure the TLS context per test
  public HttpClient httpClientWithCertificate;

  private TlsContextFactory tlsContextFactory;
  private TlsContextFactoryBuilder tlsContextFactoryBuilder = TlsContextFactory.builder();

  @Override
  protected String getConfigFile() {
    return "http-listener-validate-certificate-config.xml";
  }

  @Before
  public void setup() {
    // Configure trust store in the client with the certificate of the server.
    tlsContextFactoryBuilder.trustStorePath(getDefaultEnvironmentConfiguration().getTestGenericTrustKeyStore())
        .trustStoreType(getDefaultEnvironmentConfiguration().getTestStoreType());
    tlsContextFactoryBuilder.trustStorePassword("mulepassword");
  }

  @After
  public void after() {
    if (httpClientWithCertificate != null) {
      httpClientWithCertificate.stop();
    }
  }

  @Test
  public void serverWithValidationRejectsRequestWithInvalidCertificate() throws Exception {
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    // Send a request without configuring key store in the client.
    ExecutionException exception =
        assertThrows(ExecutionException.class, () -> sendRequest(getUrl(portWithValidation.getNumber()), TEST_MESSAGE));
    assertThat(exception.getCause(), instanceOf(IOException.class));
  }

  @Test
  public void serverWithValidationAcceptsRequestWithValidCertificate() throws Exception {
    configureClientKeyStore();
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    assertValidRequest(getUrl(portWithValidation.getNumber()));
    verify("listenerWithTrustStoreFlow");
  }

  @Test
  public void serverWithoutValidationAcceptsRequestWithInvalidCertificate() throws Exception {
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    // Send a request without configuring key store in the client.
    assertValidRequest(getUrl(portWithoutValidation.getNumber()));
  }

  @Test
  public void serverWithoutValidationAcceptsRequestWithValidCertificate() throws Exception {
    configureClientKeyStore();
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    assertValidRequest(getUrl(portWithoutValidation.getNumber()));
  }

  public void createHttpClient() {
    httpClientWithCertificate = httpService
        .client(configBuilder -> configBuilder.setName(getClass().getSimpleName()).setTlsContextFactory(tlsContextFactory));
    httpClientWithCertificate.start();
  }

  private String sendRequest(String url, String payload) throws Exception {
    HttpRequest request =
        requestBuilder().uri(url).method(POST).entity(new ByteArrayHttpEntity(payload.getBytes())).build();
    final HttpResponse response = httpClientWithCertificate
        .sendAsync(request, options -> options.setResponseTimeout(RECEIVE_TIMEOUT).setFollowsRedirect(false)).get();

    return IOUtils.toString(response.getEntity().getContent());
  }

  private void assertValidRequest(String url) throws Exception {
    assertThat(sendRequest(url, TEST_MESSAGE), equalTo(TEST_MESSAGE));
  }

  /**
   * Configure key store for the client (the server contains this certificate in its trust store)
   */
  private void configureClientKeyStore() {
    tlsContextFactoryBuilder.keyStorePath(getDefaultEnvironmentConfiguration().getTestSslKeyStore());
    tlsContextFactoryBuilder.keyStoreType(getDefaultEnvironmentConfiguration().getTestStoreType());
    tlsContextFactoryBuilder.keyStorePassword(getDefaultEnvironmentConfiguration().resolveStorePassword("changeit"));
    tlsContextFactoryBuilder.keyPassword("changeit");
  }

  private String getUrl(int port) {
    return String.format("https://localhost:%d/", port);
  }

  public static class ValidateClientCertificate extends AbstractComponent implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      HttpRequestAttributes attributes = (HttpRequestAttributes) event.getMessage().getAttributes().getValue();

      // Get CertificateData
      CertificateData certificateData = attributes.getClientCertificate();
      assertThat(certificateData, notNullValue());
      assertThat(attributes.getClientCertificate(), instanceOf(CertificateData.class));
      assertThat(attributes.getClientCertificate().getType(), is("X.509"));
      try {
        assertThat(new String(attributes.getClientCertificate().getEncoded(), UTF_8), containsString("OLEKSIYS-W3T0"));
      } catch (CertificateEncodingException encodingException) {
        fail("Encoding exception: " + encodingException);
      }

      return event;
    }
  }
}
