/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.Method.POST;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.HTTPS;
import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.functional.junit4.rules.ExpectedError;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextFactoryBuilder;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

@Features(HTTP_EXTENSION)
@Stories(HTTPS)
@Description("Sets up some HTTPS servers and clients with different protocols and ciphers. Verifies only matching configurations "
    + "are successful interacting with each other.")
public class HttpRestrictedCiphersAndProtocolsTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port1 = new DynamicPort("port1");
  @Rule
  public DynamicPort port2 = new DynamicPort("port2");
  @Rule
  public DynamicPort port3 = new DynamicPort("port3");
  @Rule
  public SystemProperty cipherSuites = new SystemProperty("cipherSuites", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA");
  @Rule
  public SystemProperty protocol = new SystemProperty("protocol", "HTTPS");
  @Rule
  public ExpectedError expectedError = ExpectedError.none();

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
    tlsContextFactoryBuilder.setTrustStorePath("tls/trustStore");
    tlsContextFactoryBuilder.setTrustStorePassword("mulepassword");
  }

  @After
  public void after() {
    if (httpClientWithCertificate != null) {
      httpClientWithCertificate.stop();
    }
  }

  @Test
  public void worksWithProtocolAndCipherSuiteMatch() throws Exception {
    Event response = flowRunner("12Client12Server").withPayload(TEST_PAYLOAD).run();
    assertThat(response.getMessage().getPayload().getValue(), is(TEST_PAYLOAD));
  }

  @Test
  public void worksWithProtocolMatch() throws Exception {
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    // Uses default ciphers and protocols
    HttpRequest request = HttpRequest.builder().uri(format("https://localhost:%s", port1.getValue())).method(POST)
        .entity(new ByteArrayHttpEntity(TEST_PAYLOAD.getBytes())).build();
    final HttpResponse response = httpClientWithCertificate.send(request, RECEIVE_TIMEOUT, false, null);
    assertThat(IOUtils.toString(response.getEntity().getContent()), is(TEST_PAYLOAD));
  }

  @Test
  public void worksWithCipherSuiteMatch() throws Exception {
    tlsContextFactoryBuilder.setEnabledCipherSuites(cipherSuites.getValue());
    tlsContextFactory = tlsContextFactoryBuilder.build();
    createHttpClient();

    // Forces TLS_DHE_DSS_WITH_AES_128_CBC_SHA
    HttpRequest request = HttpRequest.builder().uri(format("https://localhost:%s", port3.getValue())).method(POST)
        .entity(new ByteArrayHttpEntity(TEST_PAYLOAD.getBytes())).build();
    final HttpResponse response = httpClientWithCertificate.send(request, RECEIVE_TIMEOUT, false, null);
    assertThat(IOUtils.toString(response.getEntity().getContent()), is(TEST_PAYLOAD));
  }

  public void createHttpClient() {
    httpClientWithCertificate = getService(HttpService.class).getClientFactory()
        .create(new HttpClientConfiguration.Builder()
            .setName(getClass().getSimpleName())
            .setTlsContextFactory(tlsContextFactory)
            .build());
    httpClientWithCertificate.start();
  }

  @Test
  public void failsWithProtocolMismatch() throws Exception {
    expectedError.expectCause(instanceOf(HttpRequestFailedException.class));
    expectedError.expectMessage(containsString("Remotely closed"));
    flowRunner("12Client1Server").withPayload(TEST_PAYLOAD).run();
  }

  @Test
  public void failsWithCipherSuiteMismatch() throws Exception {
    expectedError.expectCause(instanceOf(HttpRequestFailedException.class));
    expectedError.expectMessage(containsString("Remotely closed"));
    flowRunner("12CipherClient1CipherServer").withPayload(TEST_PAYLOAD).run();
  }
}
