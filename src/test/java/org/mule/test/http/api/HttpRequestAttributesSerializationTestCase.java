/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;

import java.security.cert.Certificate;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for checking correct custom serialization in HttpRequestAttributes
 *
 * @since 1.4.0
 */
public class HttpRequestAttributesSerializationTestCase extends AbstractHttpAttributesTestCase {

  private static final String CERTIFICATE = "TEST_CERTIFICATE";

  private static Certificate certificateMock;

  private HttpRequestAttributesBuilder baseBuilder =
      new HttpRequestAttributesBuilder()
          .listenerPath("/listener/path")
          .relativePath("/relative/path")
          .version("1.0")
          .scheme("scheme")
          .method("GET")
          .requestPath("/request/path")
          .remoteAddress("http://10.1.2.5:8080/")
          .localAddress("http://127.0.0.1:8080/")
          .requestUri("http://127.0.0.1/gateway")
          .headers(getHeaders())
          .queryString("queryParam1=queryParam1&queryParam2=queryParam2")
          .queryParams(getQueryParams())
          .uriParams(getUriParams());

  @BeforeClass
  public static void setup() {
    certificateMock = mock(Certificate.class, withSettings().serializable());
    when(certificateMock.toString()).thenReturn(CERTIFICATE);
  }

  @Test
  public void withNoCertificate() {
    HttpRequestAttributes processed = assertSerialization(baseBuilder.build());
    assertThat(processed.getClientCertificate(), is(nullValue()));
  }

  @Test
  public void withResolvedCertificate() {
    HttpRequestAttributes processed = assertSerialization(baseBuilder.clientCertificate(certificateMock).build());
    assertThat(processed.getClientCertificate().toString(), is(CERTIFICATE));
  }

  @Test
  public void withLazyCertificate() {
    HttpRequestAttributes processed = assertSerialization(baseBuilder.clientCertificate(() -> certificateMock).build());
    assertThat(processed.getClientCertificate().toString(), is(CERTIFICATE));
  }

  private HttpRequestAttributes assertSerialization(HttpRequestAttributes original) {
    HttpRequestAttributes processed = deserialize(serialize(original));
    assertThat(processed.getListenerPath(), equalTo(original.getListenerPath()));
    assertThat(processed.getRelativePath(), equalTo(original.getRelativePath()));
    assertThat(processed.getVersion(), equalTo(original.getVersion()));
    assertThat(processed.getScheme(), equalTo(original.getScheme()));
    assertThat(processed.getMethod(), equalTo(original.getMethod()));
    assertThat(processed.getRequestPath(), equalTo(original.getRequestPath()));
    assertThat(processed.getRemoteAddress(), equalTo(original.getRemoteAddress()));
    assertThat(processed.getLocalAddress(), equalTo(original.getLocalAddress()));
    assertThat(processed.getRequestUri(), equalTo(original.getRequestUri()));
    assertThat(processed.getHeaders(), equalTo(original.getHeaders()));
    assertThat(processed.getQueryString(), equalTo(original.getQueryString()));
    assertThat(processed.getQueryParams(), equalTo(original.getQueryParams()));
    assertThat(processed.getUriParams(), equalTo(original.getUriParams()));
    assertThat(processed.getMaskedRequestPath(), equalTo(original.getMaskedRequestPath()));

    return processed;
  }
}
