/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.runtime.http.api.HttpConstants.Method.POST;
import static org.mule.tck.processor.FlowAssert.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextFactoryBuilder;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;

public class HttpListenerValidateCertificateTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort portWithValidation = new DynamicPort("port1");

  @Rule
  public DynamicPort portWithoutValidation = new DynamicPort("port2");

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
    tlsContextFactoryBuilder.trustStorePath("tls/trustStore");
    tlsContextFactoryBuilder.trustStorePassword("mulepassword");
  }

  @After
  public void after() {
    if (httpClientWithCertificate != null) {
      httpClientWithCertificate.stop();
    }
  }

  @Test(expected = IOException.class)
  public void serverWithValidationRejectsRequestWithInvalidCertificate() throws Exception {
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    // Send a request without configuring key store in the client.
    sendRequest(getUrl(portWithValidation.getNumber()), TEST_MESSAGE);
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
    httpClientWithCertificate = getService(HttpService.class).getClientFactory()
        .create(new HttpClientConfiguration.Builder()
            .setName(getClass().getSimpleName())
            .setTlsContextFactory(tlsContextFactory).build());
    httpClientWithCertificate.start();
  }

  private String sendRequest(String url, String payload) throws Exception {
    HttpRequest request =
        HttpRequest.builder().uri(url).method(POST).entity(new ByteArrayHttpEntity(payload.getBytes())).build();
    final HttpResponse response = httpClientWithCertificate.send(request, RECEIVE_TIMEOUT, false, null);

    return IOUtils.toString(response.getEntity().getContent());
  }

  private void assertValidRequest(String url) throws Exception {
    assertThat(sendRequest(url, TEST_MESSAGE), equalTo(TEST_MESSAGE));
  }

  /**
   * Configure key store for the client (the server contains this certificate in its trust store)
   */
  private void configureClientKeyStore() {
    tlsContextFactoryBuilder.keyStorePath("tls/ssltest-keystore.jks");
    tlsContextFactoryBuilder.keyStorePassword("changeit");
    tlsContextFactoryBuilder.keyPassword("changeit");
  }

  private String getUrl(int port) {
    return String.format("https://localhost:%d/", port);
  }

  public static class ValidateClientCertificate extends AbstractComponent implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      HttpRequestAttributes attributes = (HttpRequestAttributes) event.getMessage().getAttributes().getValue();
      assertThat(attributes.getClientCertificate(), notNullValue());
      assertThat(attributes.getClientCertificate(), instanceOf(java.security.cert.Certificate.class));
      try {
        // Only the client certificate contains CN=OLEKSIYS-W3T this way we validate that we are actually publishing the client
        // cert
        assertThat(new String(attributes.getClientCertificate().getEncoded(), UTF_8), containsString("OLEKSIYS-W3T0"));
      } catch (CertificateEncodingException encodingException) {
        fail("Encoding exception: " + encodingException);
      }
      return event;
    }
  }

}
