/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.fips;

/**
 * A concrete implementation of {@link TestConfiguration} used for testing in environments that do not require FIPS (Federal
 * Information Processing Standards) compliance. This class provides configurations for non-FIPS-compliant tests.
 */
public class NonFipsTestConfiguration implements TestConfiguration {

  public static final String NON_FIPS_TEST_STORE_PASSWORD = "changeit";
  public static final String NON_FIPS_TEST_STORE_TYPE = "jks";
  public static final String NON_FIPS_TEST_SSL_CA_CERTS = "tls/ssltest-cacerts.jks";
  public static final String NON_FIPS_TEST_SSL_KEY_STORE = "tls/ssltest-keystore.jks";
  public static final String NON_FIPS_TLS_SERVER_KEYSTORE = "tls/serverKeystore";
  private static final String NON_FIPS_TEST_GENERIC_TRUST_KEY_STORE = "tls/trustStore";

  public static final String NON_FIPS_TLS_CLIENT_KEYSTORE = "tls/clientKeystore";

  private static final String NON_FIPS_TLS_CLIENT_KEY_STORE_WITH_MULTIPLE_KEYS = "tls/clientServerKeystore";
  private static final String NON_FIPS_TLS_TRUST_STORE_FILE_WITHOUT_MULE_SERVER_CERTIFICATE =
      "tls/trustStoreWithoutMuleServerCertificate";

  public static final String NON_FIPS_TLS_SSLTEST_KEYSTORE_WITH_TEST_HOSTNAME = "tls/ssltest-keystore-with-test-hostname.jks";

  private static final String NON_FIPS_KEY_STORE_PK12_TYPE = "pkcs12";

  private static final String NON_FIPS_TLS_SNI_CLIENT_TRUSTSTORE = "tls/sni-client-truststore.jks";

  private static final String NON_FIPS_TRUST_STORE_JCEKS_TYPE = "jceks";

  private static final String NON_FIPS_TLS_SSLTEST_TRUSTORE_WITH_TEST_HOSTNAME = "tls/ssltest-truststore-with-test-hostname.jks";

  private static final String NON_FIPS_TESTING_TLS_13_MY_TRUSTSTORE = "tls13/myTruststore.jks";
  private static final String NON_FIPS_TESTING_TLS_13_CERT_LOCALHOST = "tls13/cert-localhost.p12";
  public static final String NON_FIPS_TEST_CIPHER_SUITE = "TLS_RSA_WITH_AES_128_CBC_SHA";

  public static final String NON_FIPS_TLS_SSLTEST_KEYSTORE_INVALID = "tls/ssltest-keystore-invalid.jks";

  @Override
  public String getTestStoreType() {
    return NON_FIPS_TEST_STORE_TYPE;
  }

  @Override
  public String getTestSslCaCerts() {
    return NON_FIPS_TEST_SSL_CA_CERTS;
  }

  @Override
  public String getTestSslKeyStore() {
    return NON_FIPS_TEST_SSL_KEY_STORE;
  }

  @Override
  public String getTestStorePassword() {
    return NON_FIPS_TEST_STORE_PASSWORD;
  }

  @Override
  public String getTestServerKeyStore() {
    return NON_FIPS_TLS_SERVER_KEYSTORE;
  }

  @Override
  public String getTestGenericTrustKeyStore() {
    return NON_FIPS_TEST_GENERIC_TRUST_KEY_STORE;
  }

  @Override
  public String getTestClientKeyStore() {
    return NON_FIPS_TLS_CLIENT_KEYSTORE;
  }

  @Override
  public String getTlsClientKeyStoreWithMultipleKeys() {
    return NON_FIPS_TLS_CLIENT_KEY_STORE_WITH_MULTIPLE_KEYS;
  }

  @Override
  public String getTlsTrustStoreFileWithoutMuleServerCertificate() {
    return NON_FIPS_TLS_TRUST_STORE_FILE_WITHOUT_MULE_SERVER_CERTIFICATE;
  }

  @Override
  public int getRandomCount() {
    return 100 * 1024;
  }

  @Override
  public String getTestSslKeyStoreWithHostName() {
    return NON_FIPS_TLS_SSLTEST_KEYSTORE_WITH_TEST_HOSTNAME;
  }

  @Override
  public String getTestSslTrustStoreWithHostName() {
    return NON_FIPS_TLS_SSLTEST_TRUSTORE_WITH_TEST_HOSTNAME;
  }

  @Override
  public String getTestSniClientTrustStore() {
    return NON_FIPS_TLS_SNI_CLIENT_TRUSTSTORE;
  }

  @Override
  public String getTestTls13TrustStore() {
    return NON_FIPS_TESTING_TLS_13_MY_TRUSTSTORE;
  }

  @Override
  public String getTestTls13KeyStore() {
    return NON_FIPS_TESTING_TLS_13_CERT_LOCALHOST;
  }

  @Override
  public String getTestCipherSuite() {
    return NON_FIPS_TEST_CIPHER_SUITE;
  }

  @Override
  public String getInvalidTestKeyStore() {
    return NON_FIPS_TLS_SSLTEST_KEYSTORE_INVALID;
  }
}
