/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.config;

import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_CONNECTION_IDLE_TIMEOUT;
import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_MAX_CONNECTIONS;
import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_RESPONSE_BUFFER_SIZE;
import static org.mule.runtime.core.api.connection.util.ConnectionProviderUtils.unwrapProviderWrapper;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.REQUEST_CONFIG;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.internal.request.ShareableHttpClient;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
@Story(REQUEST_CONFIG)
public class HttpRequestConfigTestCase extends AbstractHttpTestCase {

  private static final String DEFAULT_HTTP_REQUEST_CONFIG_NAME = "requestConfig";
  private static final String DEFAULT_PORT_HTTP_REQUEST_CONFIG_NAME = "requestConfigHttp";
  private static final String DEFAULT_PORT_HTTPS_REQUEST_CONFIG_NAME = "requestConfigHttps";
  private static final int RESPONSE_BUFFER_SIZE = 1024;
  private static final int IDLE_TIMEOUT = 10000;
  private static final int MAX_CONNECTIONS = 1;

  @Rule
  public SystemProperty bufferSize = new SystemProperty("bufferSize", String.valueOf(RESPONSE_BUFFER_SIZE));
  @Rule
  public SystemProperty maxConnections = new SystemProperty("maxConnections", String.valueOf(MAX_CONNECTIONS));
  @Rule
  public SystemProperty idleTimeout = new SystemProperty("idleTimeout", String.valueOf(IDLE_TIMEOUT));

  @Inject
  @Named("_httpRequesterConnectionManager")
  private Object connectionManager;

  @Override
  protected String getConfigFile() {
    return "http-request-config-functional-config.xml";
  }

  @Test
  public void requestConfigDefaultTlsContextHttps() throws Exception {
    ConfigurationInstance config =
        getConfigurationInstanceFromRegistry(DEFAULT_PORT_HTTPS_REQUEST_CONFIG_NAME, testEvent());
    ConnectionProvider provider = unwrapProviderWrapper(config.getConnectionProvider().get());
    assertThat(getTlsContext(provider), notNullValue());
  }

  @Test
  public void requestConfigDefaults() throws Exception {
    ConfigurationInstance config =
        getConfigurationInstanceFromRegistry(DEFAULT_HTTP_REQUEST_CONFIG_NAME, testEvent());
    ConnectionProvider provider = unwrapProviderWrapper(config.getConnectionProvider().get());
    ReflectionRequestConnectionParams connectionParams = getConnectionParams(provider);
    assertThat(connectionParams.getResponseBufferSize(), is(Integer.valueOf(DEFAULT_RESPONSE_BUFFER_SIZE)));
    assertThat(connectionParams.getMaxConnections(), is(Integer.valueOf(DEFAULT_MAX_CONNECTIONS)));
    assertThat(connectionParams.getConnectionIdleTimeout(), is(Integer.valueOf(DEFAULT_CONNECTION_IDLE_TIMEOUT)));
  }

  @Test
  public void requestConfigOverrideDefaults() throws Exception {
    ConfigurationInstance config =
        getConfigurationInstanceFromRegistry(DEFAULT_PORT_HTTP_REQUEST_CONFIG_NAME, testEvent());
    ConnectionProvider provider = unwrapProviderWrapper(config.getConnectionProvider().get());
    ReflectionRequestConnectionParams connectionParams = getConnectionParams(provider);
    assertThat(connectionParams.getResponseBufferSize(), is(RESPONSE_BUFFER_SIZE));
    assertThat(connectionParams.getMaxConnections(), is(MAX_CONNECTIONS));
    assertThat(connectionParams.getConnectionIdleTimeout(), is(IDLE_TIMEOUT));
  }

  private ReflectionRequestConnectionParams getConnectionParams(ConnectionProvider provider) {
    try {
      return new ReflectionRequestConnectionParams(provider.getClass().getMethod("getConnectionParams").invoke(provider));
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private TlsContextFactory getTlsContext(ConnectionProvider provider) {
    try {
      return (TlsContextFactory) provider.getClass().getMethod("getTlsContext").invoke(provider);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private ConfigurationInstance getConfigurationInstanceFromRegistry(String configName, CoreEvent testEvent) {
    return registry.<ConfigurationProvider>lookupByName(configName).get().get(testEvent);
  }

  @Test
  @Description("When an HttpRequesterProvider is disposed, it should be also disposed the httpClient associated with it")
  @Issue("MULE-18757")
  public void httpClientDisposed() throws Exception {
    ConfigurationInstance config =
        getConfigurationInstanceFromRegistry(DEFAULT_PORT_HTTP_REQUEST_CONFIG_NAME, testEvent());
    ConnectionProvider provider = unwrapProviderWrapper(config.getConnectionProvider().get());

    String clientId = muleContext.getConfiguration().getId() + "_" + DEFAULT_PORT_HTTP_REQUEST_CONFIG_NAME;

    assertThat(lookupConnection(clientId).isPresent(), is(true));
    ((Disposable) provider).dispose();
    assertThat(lookupConnection(clientId).isPresent(), is(false));
  }

  private Optional<ShareableHttpClient> lookupConnection(String configName) {
    try {
      Method lookupMethod = connectionManager.getClass().getMethod("lookup", String.class);
      return (Optional<ShareableHttpClient>) lookupMethod.invoke(connectionManager, configName);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private class ReflectionRequestConnectionParams {

    private final int responseBufferSize;
    private final Integer maxConnections;
    private final Integer connectionIdleTimeout;

    public ReflectionRequestConnectionParams(Object requestConnectionParams) {
      responseBufferSize = invokeNoParamsMethod(requestConnectionParams, "getResponseBufferSize");
      maxConnections = invokeNoParamsMethod(requestConnectionParams, "getMaxConnections");
      connectionIdleTimeout = invokeNoParamsMethod(requestConnectionParams, "getConnectionIdleTimeout");
    }

    public int getResponseBufferSize() {
      return responseBufferSize;
    }

    public Integer getMaxConnections() {
      return maxConnections;
    }

    public Integer getConnectionIdleTimeout() {
      return connectionIdleTimeout;
    }
  }

  private static <T> T invokeNoParamsMethod(Object object, String methodName) {
    try {
      return (T) object.getClass().getMethod(methodName).invoke(object);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
