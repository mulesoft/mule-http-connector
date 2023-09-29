/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
import static org.mule.runtime.core.api.config.i18n.CoreMessages.cannotLoadFromClasspath;
import static org.mule.runtime.core.api.util.IOUtils.getResourceAsStream;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;

import io.qameta.allure.Description;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpRequestAttributesSerializationTestCase extends AbstractHttpAttributesTestCase {

  private static Certificate certificate;

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
  public static void setup() throws Exception {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

    InputStream is = getResourceAsStream("tls/serverKeystore", HttpRequestAttributesSerializationTestCase.class);
    if (null == is) {
      throw new FileNotFoundException(cannotLoadFromClasspath("serverKeystore").getMessage());
    }

    keyStore.load(is, "mulepassword".toCharArray());
    certificate = keyStore.getCertificate("muleserver");
  }

  @Test
  @Description("HttpRequestAttributes are correctly serialized and deserialized even if no certificate was defined")
  public void withNoCertificate() {
    HttpRequestAttributes processed = assertSerialization(baseBuilder.build());
    assertThat(processed.getClientCertificate(), is(nullValue()));
  }

  @Test
  @Description("HttpRequestAttributes are correctly serialized and deserialized with an explicit certificate. Certificate can be recover after deserialization")
  public void withResolvedCertificate() {
    HttpRequestAttributes processed = assertSerialization(baseBuilder.clientCertificate(certificate).build());
    assertThat(processed.getClientCertificate(), is(certificate));
  }

  @Test
  @Description("HttpRequestAttributes are correctly serialized and deserialized with a certificate supplier. Certificate can be recover after deserialization")
  public void withLazyCertificate() {
    HttpRequestAttributes processed = assertSerialization(baseBuilder.clientCertificate(() -> certificate).build());
    assertThat(processed.getClientCertificate(), equalTo(certificate));
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
