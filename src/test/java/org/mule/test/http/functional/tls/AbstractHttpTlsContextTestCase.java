/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.tls;

import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.HTTPS;

import static java.security.Security.getProperty;
import static java.security.Security.setProperty;

import static org.apache.commons.io.FileUtils.getFile;

import org.mule.runtime.core.api.util.FileUtils;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import io.qameta.allure.Story;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.BeforeClass;

@Story(HTTPS)
public abstract class AbstractHttpTlsContextTestCase extends AbstractHttpTestCase {

  private static final String keyStorePath = "tls/ssltest-keystore.jks";
  private static final String trustStorePath = "tls/ssltest-cacerts.jks";
  private static final String storePassword = "changeit";
  private static final String keyPassword = "changeit";
  private static final String TLS12 = "TLSv1.2";
  private static final String TLS11 = "TLSv1.1";
  private static final String DISABLED_ALGORITHMS_PROPERTY = "jdk.tls.disabledAlgorithms";

  @BeforeClass
  public static void enabledProtocol() {
    setProperty(DISABLED_ALGORITHMS_PROPERTY, getProperty(DISABLED_ALGORITHMS_PROPERTY).replace(TLS11 + ", ", ""));
  }

  protected static HttpResponse executeGetRequest(String url) throws IOException, GeneralSecurityException {
    HttpClient client = getSecureClient();
    HttpGet getMethod = new HttpGet(url);
    return client.execute(getMethod);
  }

  private static HttpClient getSecureClient() throws IOException, GeneralSecurityException {
    HttpClient secureClient;
    secureClient = HttpClients.custom()
        .setSslcontext(getSslContext())
        .build();
    return secureClient;
  }

  private static SSLContext getSslContext() throws IOException, GeneralSecurityException {
    SSLContext customSslContext;
    File keyStore = getFile(FileUtils.getResourcePath(keyStorePath, AbstractHttpTlsContextTestCase.class));
    File trustStore = getFile(FileUtils.getResourcePath(trustStorePath, AbstractHttpTlsContextTestCase.class));
    char[] storePass = storePassword.toCharArray();
    char[] keyPass = keyPassword.toCharArray();
    customSslContext =
        SSLContexts.custom()
            .useProtocol(TLS12)
            .loadKeyMaterial(keyStore, storePass, keyPass)
            .loadTrustMaterial(trustStore, storePass)
            .build();
    return customSslContext;
  }
}
