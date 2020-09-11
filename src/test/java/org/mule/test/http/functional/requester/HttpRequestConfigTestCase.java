/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_CONNECTION_IDLE_TIMEOUT;
import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_MAX_CONNECTIONS;
import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_RESPONSE_BUFFER_SIZE;
import static org.mule.runtime.core.api.connection.util.ConnectionProviderUtils.unwrapProviderWrapper;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.REQUEST_CONFIG;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.getConfigurationInstanceFromRegistry;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

import org.mule.extension.http.internal.request.HttpRequesterConnectionManager;
import org.mule.extension.http.internal.request.HttpRequesterProvider;
import org.mule.extension.http.internal.request.RequestConnectionParams;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpExtensionFunctionalTestCase;

import javax.inject.Inject;

import io.qameta.allure.Feature;
import org.junit.Rule;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
@Story(REQUEST_CONFIG)
public class HttpRequestConfigTestCase extends AbstractHttpExtensionFunctionalTestCase {

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
  private HttpRequesterConnectionManager connectionManager;

  @Override
  protected String getConfigFile() {
    return "http-request-config-functional-config.xml";
  }

  @Test
  public void requestConfigDefaultTlsContextHttps() throws Exception {
    ConfigurationInstance config =
        getConfigurationInstanceFromRegistry(DEFAULT_PORT_HTTPS_REQUEST_CONFIG_NAME, testEvent(), muleContext);
    HttpRequesterProvider provider = (HttpRequesterProvider) unwrapProviderWrapper(config.getConnectionProvider().get());
    assertThat(provider.getTlsContext(), notNullValue());
  }

  @Test
  public void requestConfigDefaults() throws Exception {
    ConfigurationInstance config =
        getConfigurationInstanceFromRegistry(DEFAULT_HTTP_REQUEST_CONFIG_NAME, testEvent(), muleContext);
    HttpRequesterProvider provider = (HttpRequesterProvider) unwrapProviderWrapper(config.getConnectionProvider().get());
    RequestConnectionParams connectionParams = provider.getConnectionParams();
    assertThat(connectionParams.getResponseBufferSize(), is(Integer.valueOf(DEFAULT_RESPONSE_BUFFER_SIZE)));
    assertThat(connectionParams.getMaxConnections(), is(Integer.valueOf(DEFAULT_MAX_CONNECTIONS)));
    assertThat(connectionParams.getConnectionIdleTimeout(), is(Integer.valueOf(DEFAULT_CONNECTION_IDLE_TIMEOUT)));
  }

  @Test
  public void requestConfigOverrideDefaults() throws Exception {
    ConfigurationInstance config =
        getConfigurationInstanceFromRegistry(DEFAULT_PORT_HTTP_REQUEST_CONFIG_NAME, testEvent(), muleContext);
    HttpRequesterProvider provider = (HttpRequesterProvider) unwrapProviderWrapper(config.getConnectionProvider().get());
    RequestConnectionParams connectionParams = provider.getConnectionParams();
    assertThat(connectionParams.getResponseBufferSize(), is(RESPONSE_BUFFER_SIZE));
    assertThat(connectionParams.getMaxConnections(), is(MAX_CONNECTIONS));
    assertThat(connectionParams.getConnectionIdleTimeout(), is(IDLE_TIMEOUT));
  }

  @Test
  @Description("When an HttpRequesterProvider is disposed, it should be also disposed the httpClient associated with it")
  @Issue("MULE-18757")
  public void httpClientDisposed() throws Exception {
    ConfigurationInstance config =
        getConfigurationInstanceFromRegistry(DEFAULT_PORT_HTTP_REQUEST_CONFIG_NAME, testEvent(), muleContext);
    HttpRequesterProvider provider = (HttpRequesterProvider) unwrapProviderWrapper(config.getConnectionProvider().get());

    String clientId = muleContext.getConfiguration().getId() + "_" + DEFAULT_PORT_HTTP_REQUEST_CONFIG_NAME;

    assertThat(connectionManager.lookup(clientId).isPresent(), is(true));
    provider.dispose();
    assertThat(connectionManager.lookup(clientId).isPresent(), is(false));
  }

}
