/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.auth;

import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;
import static org.mule.test.http.functional.requester.auth.HttpRequestAuthUtils.createAuthHandler;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.util.FileUtils;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Test;
import org.slf4j.Logger;

public class HttpRequesterTestConnectivityWithAuthTestCase extends AbstractHttpRequestTestCase {

  private static final Logger LOGGER = getLogger(HttpRequesterTestConnectivityWithAuthTestCase.class);

  private int requestCount = 0;

  @Override
  protected String getConfigFile() {
    return "http-request-connectivity-test-auth-config.xml";
  }

  @Test
  public void basicAuthentication() throws MuleException {
    testAuthenticationSuccess("basicConfig");
  }

  @Test
  public void digestAuthentication() throws MuleException {
    assumeFalse("Digest authentication is based on MD5. So this should not run on FIPS",
                isFipsTesting());
    testAuthenticationSuccess("digestConfig");
  }

  @Test
  public void basicAuthenticationFailing() throws MuleException {
    testAuthenticationFailing("basicConfigFailing");
  }

  @Test
  public void digestAuthenticationFailing() throws MuleException {
    assumeFalse("Digest authentication is based on MD5. So this should not run on FIPS",
                isFipsTesting());
    testAuthenticationFailing("digestConfigFailing");
  }

  private void testAuthenticationSuccess(String configName) throws MuleException {
    // Given a requester connection provider with authentication.
    int requestCountAtBeginning = requestCount;
    ConnectionProvider connectionProvider = getConnectionProvider(configName);

    // When we validate a connection.
    Object connection = connectionProvider.connect();
    ConnectionValidationResult validationResult = connectionProvider.validate(connection);
    connectionProvider.disconnect(connection);

    // Then the validation is successful and the requests counter is incremented.
    assertThat(validationResult.getMessage(), validationResult.isValid(), is(true));
    assertThat(requestCount, is(greaterThan(requestCountAtBeginning)));
  }

  private void testAuthenticationFailing(String configName) throws MuleException {
    // Given a requester connection provider with incorrect authentication.
    ConnectionProvider connectionProvider = getConnectionProvider(configName);

    // When we validate a connection.
    Object connection = connectionProvider.connect();
    ConnectionValidationResult validationResult = connectionProvider.validate(connection);
    connectionProvider.disconnect(connection);

    // Then the validation fails.
    assertThat(validationResult.isValid(), is(false));
  }

  @Override
  protected AbstractHandler createHandler(Server server) {
    AbstractHandler handler = super.createHandler(server);
    try {
      String realmPath = FileUtils.getResourcePath("auth/realm.properties", getClass());
      return createAuthHandler(server, handler, realmPath, () -> requestCount++);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ConnectionProvider getConnectionProvider(String configName) throws MuleException {
    ConfigurationProvider configurationProvider = registry.<ConfigurationProvider>lookupByName(configName).get();
    return configurationProvider.get(testEvent()).getConnectionProvider().get();
  }
}
