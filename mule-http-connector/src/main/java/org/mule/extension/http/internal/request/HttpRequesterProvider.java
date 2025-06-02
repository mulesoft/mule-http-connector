/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.internal.HttpConnectorConstants.AUTHENTICATION;
import static org.mule.extension.http.internal.HttpConnectorConstants.TLS_CONFIGURATION;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.SECURITY_TAB;
import static org.mule.sdk.api.http.HttpConstants.Protocol.HTTP;
import static org.mule.sdk.api.http.HttpConstants.Protocol.HTTPS;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.api.request.HttpConnectivityValidator;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.api.request.proxy.HttpProxyConfig;
import org.mule.extension.http.api.request.validator.ResponseValidatorTypedException;
import org.mule.extension.http.internal.request.client.DefaultUriParameters;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.extension.socket.api.socket.tcp.TcpClientSocketProperties;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextFactoryBuilder;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.client.proxy.ProxyConfig;
import org.mule.sdk.api.http.HttpConstants;
import org.mule.sdk.api.http.client.HttpClientConfigurer;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Connection provider for a HTTP request, handles the creation of {@link HttpExtensionClient} instances.
 *
 * @since 1.0
 */
@Alias("request")
public class HttpRequesterProvider implements CachedConnectionProvider<HttpExtensionClient>, Initialisable, Disposable {

  private static final Logger LOGGER = getLogger(HttpRequesterProvider.class);

  private static final int UNLIMITED_CONNECTIONS = -1;
  private static final String NAME_PATTERN = "http.requester.%s";

  @RefName
  private String configName;

  @ParameterGroup(name = CONNECTION)
  private RequestConnectionParams connectionParams;

  /**
   * Reference to a TLS config element. This will enable HTTPS for this config.
   */
  @Parameter
  @Optional
  @Expression(NOT_SUPPORTED)
  @DisplayName(TLS_CONFIGURATION)
  @Placement(tab = SECURITY_TAB)
  private TlsContextFactory tlsContext;

  /**
   * Reusable configuration element for outbound connections through a proxy. A proxy element must define a host name and a port
   * attributes, and optionally can define a username and a password.
   */
  @Parameter
  @Optional
  @Summary("Reusable configuration element for outbound connections through a proxy")
  @Placement(tab = "Proxy")
  private HttpProxyConfig proxyConfig;

  /**
   * Authentication method to use for the HTTP request.
   */
  @Parameter
  @Optional
  @Placement(tab = AUTHENTICATION)
  private HttpRequestAuthentication authentication;

  @Parameter
  @Optional
  @Expression(NOT_SUPPORTED)
  @Placement(tab = "Connectivity Test")
  private HttpConnectivityValidator connectivityTest;

  @Inject
  private MuleContext muleContext;

  @Inject
  private HttpRequesterConnectionManager connectionManager;

  private TlsContextFactoryBuilder defaultTlsContextFactoryBuilder = TlsContextFactory.builder();

  @Override
  public ConnectionValidationResult validate(HttpExtensionClient httpClient) {
    if (connectivityTest == null) {
      // If nothing was configured, the connectivity test will be successful. This rule ensures backwards compatibility.
      return success();
    }

    try {
      connectivityTest.validate(httpClient, connectionParams);
      return success();
    } catch (ExecutionException | ResponseValidatorTypedException e) {
      return ConnectionValidationResult.failure(e.getMessage(), e);
    } catch (InterruptedException e) {
      currentThread().interrupt();
      return ConnectionValidationResult.failure(e.getMessage(), e);
    }
  }

  @Override
  public void initialise() throws InitialisationException {
    final HttpConstants.Protocol protocol = connectionParams.getProtocol();

    if (protocol.equals(HTTP) && tlsContext != null) {
      throw new InitialisationException(createStaticMessage("TlsContext cannot be configured with protocol HTTP, "
          + "when using tls:context you must set attribute protocol=\"HTTPS\""),
                                        this);
    }

    if (protocol.equals(HTTPS) && tlsContext == null) {
      // MULE-9480
      initialiseIfNeeded(defaultTlsContextFactoryBuilder);
      tlsContext = defaultTlsContextFactoryBuilder.buildDefault();
    }
    if (tlsContext != null) {
      initialiseIfNeeded(tlsContext);
    }
    if (authentication != null) {
      initialiseIfNeeded(authentication, true, muleContext);
    }
    if (connectivityTest != null) {
      initialiseIfNeeded(connectivityTest, true, muleContext);
    }

    verifyConnectionsParameters();
  }

  @Override
  public void dispose() {
    if (authentication != null) {
      disposeIfNeeded(authentication, LOGGER);
    }
    // MULE-18757: it's necessary to clean up the httpClient in case the app
    // is associated with a domain.
    connectionManager.disposeClient(getConfigurationId());
  }

  private void verifyConnectionsParameters() throws InitialisationException {
    if (connectionParams.getMaxConnections() < UNLIMITED_CONNECTIONS || connectionParams.getMaxConnections() == 0) {
      throw new InitialisationException(createStaticMessage(
                                                            "The maxConnections parameter only allows positive values or -1 for unlimited concurrent connections."),
                                        this);
    }

    if (!connectionParams.getUsePersistentConnections()) {
      connectionParams.setConnectionIdleTimeout(0);
    }
  }

  @Override
  public HttpExtensionClient connect() throws ConnectionException {
    ShareableHttpClient httpClient = connectionManager.lookupOrCreate(getConfigurationId(), this::configureClient);
    UriParameters uriParameters = new DefaultUriParameters(connectionParams.getProtocol(), connectionParams.getHost(),
                                                           connectionParams.getPort());
    HttpExtensionClient extensionClient = new HttpExtensionClient(httpClient, uriParameters, authentication);
    try {
      extensionClient.start();
    } catch (MuleException e) {
      throw new ConnectionException(e);
    }

    return extensionClient;
  }

  private void configureClient(HttpClientConfigurer configurer) {
    configurer
        .setName(format(NAME_PATTERN, configName))
        .setTlsContextFactory(tlsContext)
        .configClientSocketProperties(tcpConfigurer -> {
          TcpClientSocketProperties socketProperties = connectionParams.getClientSocketProperties();
          tcpConfigurer
              .sendBufferSize(socketProperties.getSendBufferSize())
              .sendBufferSize(socketProperties.getSendBufferSize())
              .clientTimeout(socketProperties.getClientTimeout())
              .sendTcpNoDelay(socketProperties.getSendTcpNoDelay())
              .linger(socketProperties.getLinger())
              .keepAlive(socketProperties.getKeepAlive())
              .connectionTimeout(socketProperties.getConnectionTimeout());
        })
        .setMaxConnections(connectionParams.getMaxConnections())
        .setUsePersistentConnections(connectionParams.getUsePersistentConnections())
        .setConnectionIdleTimeout(connectionParams.getConnectionIdleTimeout())
        .setStreaming(connectionParams.getStreamResponse())
        .setResponseBufferSize(connectionParams.getResponseBufferSize());

    if (authentication instanceof HttpAuthentication) {
    }

    if (proxyConfig != null) {
      configurer.configProxy(proxyConfigurer -> {
        proxyConfigurer
            .host(proxyConfig.getHost())
            .port(proxyConfig.getPort())
            .username(proxyConfig.getUsername())
            .password(proxyConfig.getPassword())
            .nonProxyHosts(proxyConfig.getNonProxyHosts());

        if (proxyConfig instanceof HttpProxyConfig.HttpNtlmProxyConfig) {
          HttpProxyConfig.HttpNtlmProxyConfig ntlmProxyConfig = (HttpProxyConfig.HttpNtlmProxyConfig) proxyConfig;
          proxyConfigurer.ntlm(ntlmProxyConfigurer -> ntlmProxyConfigurer.domain(ntlmProxyConfig.getNtlmDomain()));
        }
      });
    }
  }

  private String getConfigurationId() {
    return muleContext.getConfiguration().getId() + "_" + configName;
  }

  private org.mule.runtime.http.api.tcp.TcpClientSocketProperties buildTcpProperties(TcpClientSocketProperties socketProperties) {
    return org.mule.runtime.http.api.tcp.TcpClientSocketProperties.builder()
        .sendBufferSize(socketProperties.getSendBufferSize())
        .sendBufferSize(socketProperties.getSendBufferSize())
        .clientTimeout(socketProperties.getClientTimeout())
        .sendTcpNoDelay(socketProperties.getSendTcpNoDelay())
        .linger(socketProperties.getLinger())
        .keepAlive(socketProperties.getKeepAlive())
        .connectionTimeout(socketProperties.getConnectionTimeout())
        .build();
  }

  @Override
  public void disconnect(HttpExtensionClient httpClient) {
    try {
      httpClient.stop();
    } catch (MuleException e) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("Found exception trying to stop http client: " + e.getMessage(), e);
      }
    }
  }

  public RequestConnectionParams getConnectionParams() {
    return connectionParams;
  }

  public ProxyConfig getProxyConfig() {
    return proxyConfig;
  }

  public TlsContextFactory getTlsContext() {
    return tlsContext;
  }

  public HttpRequestAuthentication getAuthentication() {
    return authentication;
  }

  @Override
  public int hashCode() {
    return Objects.hash(authentication, configName, connectionParams, connectivityTest, proxyConfig,
                        tlsContext);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    HttpRequesterProvider other = (HttpRequesterProvider) obj;
    return Objects.equals(authentication, other.authentication)
        && Objects.equals(configName, other.configName)
        && Objects.equals(connectionParams, other.connectionParams)
        && Objects.equals(connectivityTest, other.connectivityTest)
        && Objects.equals(proxyConfig, other.proxyConfig)
        && Objects.equals(tlsContext, other.tlsContext);
  }


}
