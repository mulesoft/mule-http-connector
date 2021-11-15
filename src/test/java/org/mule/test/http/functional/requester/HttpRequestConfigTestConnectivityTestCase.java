/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.REQUEST_CONFIG;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpExtensionFunctionalTestCase;

import java.io.InputStream;
import java.util.Optional;

@Feature(HTTP_EXTENSION)
@Story(REQUEST_CONFIG)
public class HttpRequestConfigTestConnectivityTestCase extends AbstractHttpExtensionFunctionalTestCase {

  @Rule
  public DynamicPort unusedPort = new DynamicPort("unusedPort");

  @Override
  protected String getConfigFile() {
    return "http-request-config-test-connectivity.xml";
  }

  @Before
  public void setup() throws MuleException {}

  @Test
  // This test ensures that old applications which don't configure this new feature will deploy without problems.
  public void whenNoTestConnectionIsPresentTheValidationIsSuccessful() throws Exception {
    // Given a request config without test-connection.
    ConnectionProvider connectionProvider = getConnectionProvider("requestConfigWithoutTestConnection");

    // When we validate a connection.
    int irrelevantStatusCode = 400;
    HttpExtensionClient connection = mockConnection(irrelevantStatusCode);
    ConnectionValidationResult validationResult = connectionProvider.validate(connection);

    // Then the validation is successful.
    assertThat(validationResult.isValid(), is(true));
  }

  @Test
  public void whenTheConfiguredPathRespondsAnErrorTheValidationFails() throws Exception {
    // Given a request config.
    ConnectionProvider connectionProvider = getConnectionProvider("requestConfigWithOnlyPath");

    // When the validation returns a 400.
    int errorStatusCode = 400;
    HttpExtensionClient connection = mockConnection(errorStatusCode);
    ConnectionValidationResult validationResult = connectionProvider.validate(connection);

    // Then the validation is successful.
    assertThat(validationResult.isValid(), is(false));
  }

  @Test
  public void whenTheConfiguredPathRespondsAnOkTheValidationIsSuccessful() throws Exception {
    // Given a request config.
    ConnectionProvider connectionProvider = getConnectionProvider("requestConfigWithOnlyPath");

    // When the validation returns a 200.
    int okStatusCode = 200;
    HttpExtensionClient connection = mockConnection(okStatusCode);
    ConnectionValidationResult validationResult = connectionProvider.validate(connection);

    // Then the validation is successful.
    assertThat(validationResult.isValid(), is(true));
  }

  private ConnectionProvider getConnectionProvider(String configName) throws MuleException {
    ConfigurationProvider configurationProvider = registry.<ConfigurationProvider>lookupByName(configName).get();
    return configurationProvider.get(testEvent()).getConnectionProvider().get();
  }

  private HttpExtensionClient mockConnection(int responseStatusCode) {
    HttpExtensionClient client = mock(HttpExtensionClient.class);
    HttpResponse httpResponse = mockResponse(responseStatusCode);
    when(client.send(any(HttpRequest.class), anyInt(), anyBoolean(), any(HttpAuthentication.class)))
        .thenReturn(completedFuture(httpResponse));
    return client;
  }

  private HttpResponse mockResponse(int statusCode) {
    HttpEntity entity = mock(HttpEntity.class);

    String textPayload = "some text payload";
    InputStream payloadInputStream = IOUtils.toInputStream(textPayload);
    when(entity.getContent()).thenReturn(payloadInputStream);
    when(entity.getLength()).thenReturn(Optional.of((long) textPayload.length()));

    HttpResponse validationResponse = mock(HttpResponse.class);
    when(validationResponse.getStatusCode()).thenReturn(statusCode);
    when(validationResponse.getEntity()).thenReturn(entity);
    when(validationResponse.getHeaders()).thenReturn(new MultiMap<>());
    when(validationResponse.getEntity()).thenReturn(entity);
    when(validationResponse.getHeaderValue(CONTENT_TYPE)).thenReturn("text/plain");

    return validationResponse;
  }
}
