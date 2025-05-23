/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.tls;

import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.apache.commons.io.FileUtils.getFile;

import org.mule.runtime.core.api.util.FileUtils;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import io.qameta.allure.Story;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

@Story(HTTPS)
public abstract class AbstractHttpTlsContextTestCase extends AbstractHttpTestCase {

  private static final String keyPassword = "changeit";
  private static final String protocol = "TLSv1.2";

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
    File keyStore = getFile(FileUtils.getResourcePath(getDefaultEnvironmentConfiguration()
        .getTestSslKeyStore(), AbstractHttpTlsContextTestCase.class));
    File trustStore = getFile(FileUtils.getResourcePath(getDefaultEnvironmentConfiguration().getTestSslCaCerts(),
                                                        AbstractHttpTlsContextTestCase.class));
    char[] storePass = getDefaultEnvironmentConfiguration().getTestStorePassword().toCharArray();
    char[] keyPass = keyPassword.toCharArray();
    customSslContext =
        SSLContexts.custom()
            .setKeyStoreType(getDefaultEnvironmentConfiguration().getTestStoreType())
            .useProtocol(protocol)
            .loadKeyMaterial(keyStore, storePass, keyPass)
            .loadTrustMaterial(trustStore, storePass)
            .build();
    return customSslContext;
  }

}
