/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.fips;

/**
 * A concrete implementation of {@link TestConfiguration} that is used for testing in environments with FIPS (Federal Information
 * Processing Standards) compliance. This class provides configurations specific to FIPS-compliant tests.
 */
public class FipsTestConfiguration implements TestConfiguration {

  public static final String FIPS_TEST_STORE_PASSWORD = "mulepassword";
  public static final String FIPS_TEST_STORE_TYPE = "bcfks";
  public static final String FIPS_TEST_SSL_CA_CERTS = "tls/ssltest-cacerts-fips.bcfks";
  public static final String FIPS_TEST_SSL_KEY_STORE = "tls/ssltest-keystore-fips.bcfks";
  public static final String FIPS_TLS_SERVER_KEYSTORE = "tls/serverKeystoreFips";
  private static final String FIPS_TEST_GENERIC_TRUST_KEY_STORE = "tls/trustStoreFips";
  private static final String FIPS_TLS_CLIENT_KEYSTORE = "tls/clientKeystoreFips";
  private static final String FIPS_TLS_CLIENT_KEY_STORE_WITH_MULTIPLE_KEYS = "tls/clientServerKeyStoreFips";
  private static final String FIPS_TLS_TRUST_STORE_FILE_WITHOUT_MULE_SERVER_CERTIFICATE =
      "tls/trustStoreWithoutMuleServerCertificateFips";
  private static final String FIPS_TLS_SSLTEST_KEYSTORE_WITH_TEST_HOSTNAME = "tls/ssltest-keystore-with-test-hostname-fips.bcfks";

  private static final String FIPS_TLS_SSLTEST_TRUSTORE_WITH_TEST_HOSTNAME =
      "tls/ssltest-truststore-with-test-hostname-fips.bcfks";
  private static final String FIPS_TLS_SNI_CLIENT_TRUSTSTORE = "tls/sni-client-truststore-fips.bcfks";
  private static final String FIPS_TESTING_TLS_13_MY_TRUSTSTORE = "tls13/myTruststoreFips.bcfks";
  private static final String FIPS_TESTING_TLS_13_CERT_LOCALHOST = "tls13/cert-localhost-fips.bcfks";
  public static final String FIPS_TEST_CIPHER_SUITE = "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";
  public static final String FIPS_TLS_SSLTEST_KEYSTORE_INVALID = "tls/ssltest-keystore-invalid.bcfks";

  @Override
  public String getTestStoreType() {
    return FIPS_TEST_STORE_TYPE;
  }

  @Override
  public String getTestSslCaCerts() {
    return FIPS_TEST_SSL_CA_CERTS;
  }

  @Override
  public String getTestSslKeyStore() {
    return FIPS_TEST_SSL_KEY_STORE;
  }

  @Override
  public String getTestStorePassword() {
    return FIPS_TEST_STORE_PASSWORD;
  }

  @Override
  public String getTestServerKeyStore() {
    return FIPS_TLS_SERVER_KEYSTORE;
  }

  @Override
  public String getTestGenericTrustKeyStore() {
    return FIPS_TEST_GENERIC_TRUST_KEY_STORE;
  }

  @Override
  public String getTestClientKeyStore() {
    return FIPS_TLS_CLIENT_KEYSTORE;
  }

  @Override
  public String getTlsClientKeyStoreWithMultipleKeys() {
    return FIPS_TLS_CLIENT_KEY_STORE_WITH_MULTIPLE_KEYS;
  }

  @Override
  public String getTlsTrustStoreFileWithoutMuleServerCertificate() {
    return FIPS_TLS_TRUST_STORE_FILE_WITHOUT_MULE_SERVER_CERTIFICATE;
  }

  @Override
  public int getRandomCount() {
    return 10 * 1024;
  }

  @Override
  public String resolveStorePassword(String defaultPassword) {
    return FIPS_TEST_STORE_PASSWORD;
  }

  @Override
  public String getTestSslKeyStoreWithHostName() {
    return FIPS_TLS_SSLTEST_KEYSTORE_WITH_TEST_HOSTNAME;
  }

  @Override
  public String getTestSslTrustStoreWithHostName() {
    return FIPS_TLS_SSLTEST_TRUSTORE_WITH_TEST_HOSTNAME;
  }

  @Override
  public String getTestSniClientTrustStore() {
    return FIPS_TLS_SNI_CLIENT_TRUSTSTORE;
  }

  @Override
  public String getTestTls13TrustStore() {
    return FIPS_TESTING_TLS_13_MY_TRUSTSTORE;
  }

  @Override
  public String getTestTls13KeyStore() {
    return FIPS_TESTING_TLS_13_CERT_LOCALHOST;
  }

  @Override
  public String getTestCipherSuite() {
    return FIPS_TEST_CIPHER_SUITE;
  }

  @Override
  public String getInvalidTestKeyStore() {
    return FIPS_TLS_SSLTEST_KEYSTORE_INVALID;
  }
}
