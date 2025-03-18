/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.fips;

/**
 * Interface representing a configuration for testing. Provides methods to retrieve information about store types, SSL
 * certificates, key stores, and store passwords used during testing.
 */
public interface TestConfiguration {

  /**
   * Retrieves the type of the test store.
   *
   * @return a {@link String} representing the type of the test store.
   */
  String getTestStoreType();

  /**
   * Retrieves the CA certificates used for SSL during testing.
   *
   * @return a {@link String} representing the CA certificates for SSL.
   */
  String getTestSslCaCerts();

  /**
   * Retrieves the key store used for SSL during testing.
   *
   * @return a {@link String} representing the SSL key store.
   */
  String getTestSslKeyStore();

  /**
   * Retrieves the password for the test store.
   *
   * @return a {@link String} representing the test store password.
   */
  String getTestStorePassword();

  /**
   * Retrieves the server key store used for testing SSL connections.
   *
   * @return a {@link String} representing the server SSL key store.
   */
  String getTestServerKeyStore();

  /**
   * Retrieves the generic trust key store used for testing SSL connections.
   *
   * @return a {@link String} representing the generic trust SSL key store.
   */
  String getTestGenericTrustKeyStore();

  /**
   * Retrieves the key store for the test client.
   *
   * @return a {@code String} representing the test client's key store.
   */
  String getTestClientKeyStore();

  /**
   * Retrieves the key store for the TLS client that contains multiple keys.
   *
   * @return a {@code String} representing the TLS client's key store with multiple keys.
   */
  String getTlsClientKeyStoreWithMultipleKeys();

  /**
   * Retrieves the trust store file that excludes the Mule server certificate.
   *
   * @return a {@code String} representing the trust store file without the Mule server certificate.
   */
  String getTlsTrustStoreFileWithoutMuleServerCertificate();

  /**
   * Retrieves a random count value.
   *
   * @return an {@code int} representing a random count.
   */
  int getRandomCount();

  /**
   * Resolves the password according to the environment.
   *
   * @param defaultPassword the default password
   * @return the resolvedPassword.
   */
  default String resolveStorePassword(String defaultPassword) {
    return defaultPassword;
  }

  /**
   * Retrieves the SSL key store for testing, associated with a specific host name.
   *
   * @return a {@code String} representing the test SSL key store with the associated host name.
   */
  String getTestSslKeyStoreWithHostName();

  /**
   * Retrieves the SSL trust store for testing, associated with a specific host name.
   *
   * @return a {@code String} representing the test SSL trust store with the associated host name.
   */

  String getTestSslTrustStoreWithHostName();

  /**
   * Retrieves the trust store for the test client using Server Name Indication (SNI).
   *
   * @return a {@code String} representing the test SNI client trust store.
   */
  String getTestSniClientTrustStore();

  /**
   * Returns the trust store to be used for TLS 1.3 testing. The trust store contains the trusted certificates that will be used
   * during the secure communication in TLS 1.3 tests.
   *
   * @return the file path or identifier of the TLS 1.3 test trust store.
   */
  String getTestTls13TrustStore();

  /**
   * Returns the key store to be used for TLS 1.3 testing. The key store contains the private keys and certificates that will be
   * used for establishing secure communication in TLS 1.3 tests.
   *
   * @return the file path or identifier of the TLS 1.3 test key store.
   */
  String getTestTls13KeyStore();

  /**
   * Returns the cipher suite to be used for testing. A cipher suite defines the algorithms that will be used for encryption,
   * authentication, and key exchange during secure communication in tests.
   *
   * @return the name of the test cipher suite.
   */
  String getTestCipherSuite();

  /**
   * Returns an invalid or unsupported cipher suite for testing purposes. This can be used to simulate scenarios where an
   * incorrect or incompatible cipher suite is provided, helping to test error handling and fallback mechanisms.
   *
   * @return the name of the invalid test cipher suite.
   */
  String getInvalidTestKeyStore();
}

