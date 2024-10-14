/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.fips;

import static java.lang.Boolean.getBoolean;

public class DefaultTestConfiguration implements TestConfiguration {

  public static DefaultTestConfiguration getDefaultEnvironmentConfiguration() {
    return new DefaultTestConfiguration();
  }

  public static final String NAME_TESTING_SYS_PROP = "mule.fips.testing";

  public static boolean isFipsTesting() {
    return getBoolean(NAME_TESTING_SYS_PROP);
  }

  private final TestConfiguration delegate;

  private DefaultTestConfiguration() {
    this.delegate = resolveTestEnvironmentConfiguration();
  }

  private TestConfiguration resolveTestEnvironmentConfiguration() {
    if (isFipsTesting()) {
      return new FipsTestConfiguration();
    }
    return new NonFipsTestConfiguration();
  }

  @Override
  public String getTestStoreType() {
    return delegate.getTestStoreType();
  }

  @Override
  public String getTestSslCaCerts() {
    return delegate.getTestSslCaCerts();
  }

  @Override
  public String getTestSslKeyStore() {
    return delegate.getTestSslKeyStore();
  }

  @Override
  public String getTestStorePassword() {
    return delegate.getTestStorePassword();
  }

  @Override
  public String getTestServerKeyStore() {
    return delegate.getTestServerKeyStore();
  }

  @Override
  public String getTestGenericTrustKeyStore() {
    return delegate.getTestGenericTrustKeyStore();
  }

  @Override
  public String getTestClientKeyStore() {
    return delegate.getTestClientKeyStore();
  }

  @Override
  public String getTlsClientKeyStoreWithMultipleKeys() {
    return delegate.getTlsClientKeyStoreWithMultipleKeys();
  }

  @Override
  public String getTlsTrustStoreFileWithoutMuleServerCertificate() {
    return delegate.getTlsTrustStoreFileWithoutMuleServerCertificate();
  }

  @Override
  public int getRandomCount() {
    return delegate.getRandomCount();
  }

  @Override
  public String getKeyStorePKS12Type() {
    return delegate.getKeyStorePKS12Type();
  }

  @Override
  public String getTrustStoreJCEKSType() {
    return delegate.getTrustStoreJCEKSType();
  }

  @Override
  public String getCertificateAuthorityEntity() {
    return delegate.getCertificateAuthorityEntity();
  }

  @Override
  public String getTrustFileForCrl() {
    return delegate.getTrustFileForCrl();
  }

  @Override
  public String getTestSslKeyStoreWithHostName() {
    return delegate.getTestSslKeyStoreWithHostName();
  }

  @Override
  public String getTestSslTrustStoreWithHostName() {
    return delegate.getTestSslTrustStoreWithHostName();
  }

  @Override
  public String getTestSniClientTrustStore() {
    return delegate.getTestSniClientTrustStore();
  }

  @Override
  public String getTestTls13TrustStore() {
    return delegate.getTestTls13TrustStore();
  }

  @Override
  public String getTestTls13KeyStore() {
    return delegate.getTestTls13KeyStore();
  }

  @Override
  public String resolveStorePassword(String defaultPassword) {
    return delegate.resolveStorePassword(defaultPassword);
  }

  @Override
  public String getTestCipherSuite() {
    return delegate.getTestCipherSuite();
  }

  @Override
  public String getInvalidTestKeyStore() {
    return delegate.getInvalidTestKeyStore();
  }
}
