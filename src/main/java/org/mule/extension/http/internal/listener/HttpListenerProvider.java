/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static java.lang.String.format;
import static org.mule.extension.http.internal.HttpConnectorConstants.TLS_CONFIGURATION;
import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.ADVANCED;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.SECURITY_TAB;
import static org.mule.runtime.http.api.HttpConstants.Protocol.HTTP;
import static org.mule.runtime.http.api.HttpConstants.Protocol.HTTPS;

import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.api.notification.NotificationListenerRegistry;
import org.mule.runtime.api.scheduler.SchedulerConfig;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.server.HttpServer;
import org.mule.runtime.http.api.server.HttpServerConfiguration;
import org.mule.runtime.http.api.server.ServerAddress;
import org.mule.runtime.http.api.server.ServerCreationException;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.inject.Inject;

/**
 * Connection provider for a {@link HttpListener}, handles the creation of {@link HttpServer} instances.
 *
 * @since 1.0
 */
@Alias("listener")
public class HttpListenerProvider implements CachedConnectionProvider<HttpServer>, Lifecycle {

  public static final class ConnectionParams {

    /**
     * Protocol to use for communication. Valid values are HTTP and HTTPS. Default value is HTTP. When using HTTPS the HTTP
     * communication is going to be secured using TLS / SSL. If HTTPS was configured as protocol then the user needs to configure
     * at least the keystore in the tls:context child element of this listener-config.
     */
    @Parameter
    @Optional(defaultValue = "HTTP")
    @Expression(NOT_SUPPORTED)
    @Placement(order = 1)
    private HttpConstants.Protocol protocol;

    /**
     * Host where the requests will be sent.
     */
    @Parameter
    @Example("0.0.0.0")
    @Expression(NOT_SUPPORTED)
    @Placement(order = 2)
    private String host;

    /**
     * Port where the requests will be received.
     */
    @Parameter
    @Example("8081")
    @Expression(NOT_SUPPORTED)
    @Placement(order = 3)
    private Integer port;

    /**
     * If false, each connection will be closed after the first request is completed.
     */
    @Parameter
    @Optional(defaultValue = "true")
    @Expression(NOT_SUPPORTED)
    @Placement(tab = ADVANCED, order = 1)
    private boolean usePersistentConnections;

    /**
     * The number of milliseconds that a connection can remain idle before it is closed. The value of this attribute is only used
     * when persistent connections are enabled.
     */
    @Parameter
    @Optional(defaultValue = "30000")
    @Expression(NOT_SUPPORTED)
    @Placement(tab = ADVANCED, order = 2)
    private Integer connectionIdleTimeout;

    public HttpConstants.Protocol getProtocol() {
      return protocol;
    }

    public String getHost() {
      return host;
    }

    public Integer getPort() {
      return port;
    }

    public boolean getUsePersistentConnections() {
      return usePersistentConnections;
    }

    public Integer getConnectionIdleTimeout() {
      return connectionIdleTimeout;
    }

  }

  @RefName
  private String configName;

  @ParameterGroup(name = ParameterGroup.CONNECTION)
  private ConnectionParams connectionParams;

  /**
   * Reference to a TLS config element. This will enable HTTPS for this config.
   */
  @Parameter
  @Optional
  @Expression(NOT_SUPPORTED)
  @DisplayName(TLS_CONFIGURATION)
  @Placement(tab = SECURITY_TAB)
  private TlsContextFactory tlsContext;

  @Inject
  private HttpService httpService;

  @Inject
  private SchedulerService schedulerService;

  @Inject
  private NotificationListenerRegistry notificationListenerRegistry;

  private MuleContextStopWatcher muleContextStopWatcher;
  private HttpServer server;

  @Override
  public void initialise() throws InitialisationException {

    if (connectionParams.port == null) {
      connectionParams.port = connectionParams.protocol.getDefaultPort();
    }

    if (connectionParams.protocol.equals(HTTP) && tlsContext != null) {
      throw new InitialisationException(createStaticMessage("TlsContext cannot be configured with protocol HTTP. "
          + "If you defined a tls:context element in your listener-config then you must set protocol=\"HTTPS\""), this);
    }
    if (connectionParams.protocol.equals(HTTPS) && tlsContext == null) {
      throw new InitialisationException(createStaticMessage("Configured protocol is HTTPS but there's no TlsContext configured for configuration '%s'.",
                                                            configName),
                                        this);
    }
    if (tlsContext != null && !tlsContext.isKeyStoreConfigured()) {
      throw new InitialisationException(createStaticMessage("KeyStore must be configured for server side SSL in configuration '%s'.",
                                                            configName),
                                        this);
    }

    if (tlsContext != null) {
      initialiseIfNeeded(tlsContext);
    }

    verifyConnectionsParameters();

    HttpServerConfiguration serverConfiguration = getServerConfiguration();

    try {
      server = httpService.getServerFactory().create(serverConfiguration);
    } catch (ServerCreationException e) {
      throw new InitialisationException(createStaticMessage(buildFailureMessage("create", e)), e, this);
    }

    if (muleContextStopWatcher == null) {
      muleContextStopWatcher = new MuleContextStopWatcher();
      notificationListenerRegistry.registerListener(muleContextStopWatcher);
    }
  }

  private HttpServerConfiguration getServerConfiguration() {
    HttpServerConfiguration.Builder builder = new HttpServerConfiguration.Builder()
        .setHost(connectionParams.getHost())
        .setPort(connectionParams.getPort())
        .setTlsContextFactory(tlsContext).setUsePersistentConnections(connectionParams.getUsePersistentConnections())
        .setConnectionIdleTimeout(connectionParams.getConnectionIdleTimeout())
        .setName(configName);

    if (useIOScheduler()) {
      builder.setSchedulerSupplier(() -> schedulerService
          .ioScheduler(SchedulerConfig.config().withName(getSchedulerName(connectionParams))));
    }

    return builder.build();
  }

  private boolean useIOScheduler() {
    try {
      Field result = httpService.getServerFactory().getClass().getDeclaredField("USE_IO_SCHEDULER");
      return result.getBoolean(httpService);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return false;
    }
  }

  private String getSchedulerName(ConnectionParams connectionParams) {
    return format("http-listener-scheduler-io[%s://%s:%d]", connectionParams.getProtocol().getScheme(),
                  connectionParams.getHost(), connectionParams.getPort());
  }

  @Override
  public void start() throws MuleException {
    try {
      server.start();
    } catch (IOException e) {
      throw new DefaultMuleException(new ConnectionException(buildFailureMessage("start", e), e));
    }
  }

  private String buildFailureMessage(final String action, Throwable e) {
    StringBuilder builder = new StringBuilder()
        .append("Could not ")
        .append(action)
        .append(" HTTP server for '")
        .append(configName)
        .append("' on port ")
        .append(connectionParams.port);
    if (e.getMessage() != null) {
      builder
          .append(": ")
          .append(e.getMessage());
    }
    return builder.toString();
  }

  @Override
  public void stop() throws MuleException {
    if (!server.isStopped()) {
      server.stop();
    }
  }

  @Override
  public void dispose() {
    server.dispose();
  }

  @Override
  public HttpServer connect() throws ConnectionException {
    return new HttpServerDelegate(server) {

      @Override
      public HttpServer stop() {
        if (muleContextStopWatcher.isStopping()) {
          super.stop();
        }
        return this;
      }
    };
  }

  @Override
  public void disconnect(HttpServer server) {
    // server could be shared with other listeners, do nothing
  }

  @Override
  public ConnectionValidationResult validate(HttpServer server) {
    if (server.isStopped() || server.isStopping()) {
      ServerAddress serverAddress = server.getServerAddress();
      return failure(format("Server on host %s and port %s is stopped.", serverAddress.getIp(), serverAddress.getPort()),
                     new ConnectionException("Server stopped."));
    } else {
      return ConnectionValidationResult.success();
    }
  }

  public ConnectionParams getConnectionParams() {
    return connectionParams;
  }

  private void verifyConnectionsParameters() throws InitialisationException {
    if (!connectionParams.getUsePersistentConnections()) {
      connectionParams.connectionIdleTimeout = 0;
    }
  }
}
