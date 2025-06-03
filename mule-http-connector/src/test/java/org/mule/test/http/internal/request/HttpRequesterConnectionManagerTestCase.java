/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.internal.request;

import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.internal.request.HttpRequesterConnectionManager;
import org.mule.extension.http.internal.request.ShareableHttpClient;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.sdk.api.http.HttpService;
import org.mule.sdk.api.http.client.HttpClient;
import org.mule.sdk.api.http.client.HttpClientConfig;
import org.mule.sdk.api.http.client.proxy.ProxyConfig;
import org.mule.sdk.api.http.tcp.TcpSocketPropertiesConfigurer;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.function.Consumer;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.slf4j.Logger;

@Feature(HTTP_EXTENSION)
@Story("HTTP Request")
public class HttpRequesterConnectionManagerTestCase extends AbstractMuleTestCase {

  private final Logger LOGGER = getLogger(HttpRequesterConnectionManagerTestCase.class);

  private static final String CONFIG_NAME = "config";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private HttpService httpService = mock(HttpService.class);
  private HttpClient delegateHttpClient = spy(HttpClient.class);
  private HttpClient otherHttpClient = spy(HttpClient.class);
  private HttpRequesterConnectionManager connectionManager = new HttpRequesterConnectionManager(httpService);

  @Before
  public void setUp() {
    when(httpService.client(any())).thenAnswer(
                                               invocation -> {
                                                 Consumer<HttpClientConfig> consumer =
                                                     (Consumer<HttpClientConfig>) invocation.getArguments()[0];
                                                 TestConfig configurer = new TestConfig();
                                                 consumer.accept(configurer);
                                                 if (CONFIG_NAME.equals(configurer.name)) {
                                                   return delegateHttpClient;
                                                 } else {
                                                   return otherHttpClient;
                                                 }
                                               });
  }

  @Test
  public void sharedClientIsStartedByFirstUse() {
    ShareableHttpClient client1 = connectionManager.lookupOrCreate(CONFIG_NAME, cfg -> {
    });
    ShareableHttpClient client2 = connectionManager.lookupOrCreate(CONFIG_NAME, cfg -> {
    });
    client1.start();
    verify(delegateHttpClient).start();
    reset(delegateHttpClient);
    client2.start();
    verify(delegateHttpClient, never()).start();
  }

  @Test
  public void sharedClientIsStoppedByLastUse() {
    ShareableHttpClient client1 = connectionManager.lookupOrCreate(CONFIG_NAME, cfg -> {
    });
    ShareableHttpClient client2 = connectionManager.lookupOrCreate(CONFIG_NAME, cfg -> {
    });
    client1.start();
    client2.start();
    client1.stop();
    verify(delegateHttpClient, never()).stop();
    reset(delegateHttpClient);
    client2.stop();
    verify(delegateHttpClient).stop();
  }

  @Test
  public void differentClientsDoNotAffectEachOther() {
    ShareableHttpClient client1 = connectionManager.lookupOrCreate(CONFIG_NAME, cfg -> {
    });
    String otherConfig = "otherConfig";
    connectionManager.lookupOrCreate(otherConfig, cfg -> {
    });
    client1.start();
    verify(otherHttpClient, never()).start();
    client1.stop();
    verify(otherHttpClient, never()).stop();
  }

  @Test
  public void clientIsStartedAfterFirstError() {
    doThrow(RuntimeException.class).doNothing().when(delegateHttpClient).start();
    ShareableHttpClient client = connectionManager.lookupOrCreate(CONFIG_NAME, cfg -> {
    });
    try {
      client.start();
    } catch (RuntimeException e) {
      // Ignore first exception
    }
    client.start();
    verify(delegateHttpClient, Mockito.times(2)).start();
  }

  private static class TestConfig implements HttpClientConfig {

    private TlsContextFactory tlsContextFactory;
    private int maxConnections;
    private boolean usePersistentConnections;
    private int connectionIdleTimeout;
    private boolean streaming;
    private int responseBufferSize;
    private String name;
    private Boolean decompress;
    private Consumer<TcpSocketPropertiesConfigurer> tcpConfigurer;
    private Consumer<ProxyConfig> proxyConfigurer;

    @Override
    public HttpClientConfig setTlsContextFactory(TlsContextFactory tlsContextFactory) {
      this.tlsContextFactory = tlsContextFactory;
      return this;
    }

    @Override
    public HttpClientConfig setMaxConnections(int maxConnections) {
      this.maxConnections = maxConnections;
      return this;
    }

    @Override
    public HttpClientConfig setUsePersistentConnections(boolean usePersistentConnections) {
      this.usePersistentConnections = usePersistentConnections;
      return this;
    }

    @Override
    public HttpClientConfig setConnectionIdleTimeout(int connectionIdleTimeout) {
      this.connectionIdleTimeout = connectionIdleTimeout;
      return this;
    }

    @Override
    public HttpClientConfig setStreaming(boolean streaming) {
      this.streaming = streaming;
      return this;
    }

    @Override
    public HttpClientConfig setResponseBufferSize(int responseBufferSize) {
      this.responseBufferSize = responseBufferSize;
      return this;
    }

    @Override
    public HttpClientConfig setName(String name) {
      this.name = name;
      return this;
    }

    @Override
    public HttpClientConfig setDecompress(Boolean decompress) {
      this.decompress = decompress;
      return this;
    }

    @Override
    public HttpClientConfig configClientSocketProperties(Consumer<TcpSocketPropertiesConfigurer> configurer) {
      this.tcpConfigurer = configurer;
      return this;
    }

    @Override
    public HttpClientConfig configProxy(Consumer<ProxyConfig> configurer) {
      this.proxyConfigurer = configurer;
      return this;
    }
  }
}
