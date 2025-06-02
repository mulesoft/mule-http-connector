/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.sdk.api.http.HttpConstants.Method.POST;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;

import static java.lang.String.format;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextFactoryBuilder;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.sdk.api.http.client.HttpClient;
import org.mule.sdk.api.http.domain.entity.ByteArrayHttpEntity;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.http.domain.message.response.HttpResponse;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.http.functional.fips.DefaultTestConfiguration;

import io.qameta.allure.Story;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Story(HTTPS)
@DisplayName("HTTPS Restricted Ciphers and Protocols")
public class HttpRestrictedCiphersAndProtocolsTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port1 = new DynamicPort("port1");
  @Rule
  public DynamicPort port2 = new DynamicPort("port2");
  @Rule
  public DynamicPort port3 = new DynamicPort("port3");
  @Rule
  public SystemProperty cipherSuites = new SystemProperty("cipherSuites", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA");
  @Rule
  public SystemProperty protocol = new SystemProperty("protocol", "HTTPS");
  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  @Rule
  public SystemProperty trustStoreFile = new SystemProperty("trustStore", getStorePath());

  @Rule
  public SystemProperty storeType = new SystemProperty("storeType", getDefaultEnvironmentConfiguration().getTestStoreType());

  @Rule
  public SystemProperty serverKeyStore = new SystemProperty("serverKeyStore", getServerKeyStore());

  // Uses a new HttpClient because it is needed to configure the TLS context per test
  public HttpClient httpClientWithCertificate;

  private TlsContextFactory tlsContextFactory;
  private TlsContextFactoryBuilder tlsContextFactoryBuilder = TlsContextFactory.builder();

  @Override
  protected String getConfigFile() {
    return "http-restricted-ciphers-and-protocols-config.xml";
  }

  @Before
  public void setUp() {
    tlsContextFactoryBuilder.trustStorePath(getStorePath());
    tlsContextFactoryBuilder.trustStorePassword("mulepassword");
    tlsContextFactoryBuilder.trustStoreType(getDefaultEnvironmentConfiguration().getTestStoreType());
  }

  @After
  public void after() {
    if (httpClientWithCertificate != null) {
      httpClientWithCertificate.stop();
    }
  }

  @Test
  public void worksWithProtocolAndCipherSuiteMatch() throws Exception {
    CoreEvent response = flowRunner("12Client12Server").withPayload(TEST_PAYLOAD).run();
    assertThat(response.getMessage().getPayload().getValue(), is(TEST_PAYLOAD));
  }

  @Test
  @Ignore("Failing because of JDK defaults")
  public void worksWithProtocolMatch() throws Exception {
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    // Uses default ciphers and protocols
    HttpRequest request = requestBuilder()
        .method(POST)
        .uri(format("https://localhost:%s", port1.getValue()))
        .entity(new ByteArrayHttpEntity(TEST_PAYLOAD.getBytes()))
        .build();
    final HttpResponse response = httpClientWithCertificate
        .sendAsync(request, options -> options.setResponseTimeout(RECEIVE_TIMEOUT).setFollowsRedirect(false)).get();
    assertThat(IOUtils.toString(response.getEntity().getContent()), is(TEST_PAYLOAD));
  }

  @Test
  public void worksWithCipherSuiteMatch() throws Exception {
    tlsContextFactoryBuilder.enabledCipherSuites(cipherSuites.getValue());
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    // Forces TLS_DHE_RSA_WITH_AES_128_CBC_SHA
    HttpRequest request = requestBuilder()
        .method(POST)
        .uri(format("https://localhost:%s", port3.getValue()))
        .entity(new ByteArrayHttpEntity(TEST_PAYLOAD.getBytes()))
        .build();
    final HttpResponse response = httpClientWithCertificate
        .sendAsync(request, options -> options.setResponseTimeout(RECEIVE_TIMEOUT).setFollowsRedirect(false)).get();
    assertThat(IOUtils.toString(response.getEntity().getContent()), is(TEST_PAYLOAD));
  }

  public void createHttpClient() {
    httpClientWithCertificate = httpService
        .client(config -> config
            .setName(getClass().getSimpleName())
            .setTlsContextFactory(tlsContextFactory));
    httpClientWithCertificate.start();
  }

  @Test
  public void failsWithProtocolMismatch() throws Exception {
    expectedError.expectCause(instanceOf(HttpRequestFailedException.class));
    expectedError.expectMessage(anyOf(containsString("Remotely closed"), sslValidationError()));
    flowRunner("12Client1Server").withPayload(TEST_PAYLOAD).run();
  }

  @Test
  public void failsWithCipherSuiteMismatch() throws Exception {
    expectedError.expectCause(instanceOf(HttpRequestFailedException.class));
    expectedError.expectMessage(anyOf(containsString("Remotely closed"), sslValidationError()));
    flowRunner("12CipherClient1CipherServer").withPayload(TEST_PAYLOAD).run();
  }

  private static String getStorePath() {
    if (isFipsTesting()) {
      return "tls/trustStoreFips";
    }

    return "tls/trustStore";
  }

  private static String getServerKeyStore() {
    if (DefaultTestConfiguration.isFipsTesting()) {
      return "tls/serverKeystoreFips";
    }
    return "tls/serverKeystore";
  }
}
