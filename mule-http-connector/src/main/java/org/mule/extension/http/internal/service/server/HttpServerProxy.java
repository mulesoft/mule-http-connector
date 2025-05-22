/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import static org.mule.extension.http.internal.service.server.HttpServerProxy.HttpServerForMuleApi.getServerConfigurationForMuleApi;
import static org.mule.extension.http.internal.service.server.HttpServerProxy.HttpServerForSdkApi.configureServerBuilderForSdkApi;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.util.ClassUtils.getMethod;

import static java.lang.Long.parseLong;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.internal.listener.HttpListenerProvider;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.http.api.server.HttpServerConfiguration;
import org.mule.runtime.http.api.server.ServerCreationException;
import org.mule.sdk.api.http.server.HttpServer;
import org.mule.sdk.api.http.server.HttpServerConfigurer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;

public interface HttpServerProxy {

  static HttpServerProxy forMuleApi(org.mule.runtime.http.api.HttpService muleService, String configName,
                                    HttpListenerProvider.ConnectionParams connectionParams, TlsContextFactory tlsContext,
                                    Supplier<Scheduler> ioSchedulerSupplier)
      throws ServerCreationException {
    return new HttpServerForMuleApi(muleService.getServerFactory()
        .create(getServerConfigurationForMuleApi(configName, connectionParams, tlsContext, ioSchedulerSupplier)));
  }

  static HttpServerProxy forSdkApi(org.mule.sdk.api.http.HttpService sdkApiService, String configName,
                                   HttpListenerProvider.ConnectionParams connectionParams, TlsContextFactory tlsContext,
                                   Supplier<Scheduler> ioSchedulerSupplier)
      throws ServerCreationException {
    try {
      return new HttpServerProxy.HttpServerForSdkApi(sdkApiService
          .server(configurer -> configureServerBuilderForSdkApi(configurer, configName, connectionParams, tlsContext,
                                                                ioSchedulerSupplier)));
    } catch (org.mule.sdk.api.http.server.ServerCreationException e) {
      throw new ServerCreationException("Delegation error", e);
    }
  }

  void start() throws IOException;

  void stop();

  boolean isStopped();

  boolean isStopping();

  void dispose();

  String getIp();

  int getPort();

  EndpointAvailabilityManager addRequestHandler(List<String> list, String path, RequestHandlerProxy requestHandler);

  EndpointAvailabilityManager addRequestHandler(String path, RequestHandlerProxy requestHandler);

  class HttpServerForSdkApi implements HttpServerProxy {

    private final HttpServer delegate;

    public HttpServerForSdkApi(HttpServer delegate) {
      this.delegate = delegate;
    }

    @Override
    public void start() throws IOException {
      delegate.start();
    }

    @Override
    public void stop() {
      delegate.stop();
    }

    @Override
    public boolean isStopped() {
      return delegate.isStopped();
    }

    @Override
    public boolean isStopping() {
      return delegate.isStopping();
    }

    @Override
    public void dispose() {
      delegate.dispose();
    }

    @Override
    public String getIp() {
      return delegate.getServerAddress().getIp();
    }

    @Override
    public int getPort() {
      return delegate.getServerAddress().getPort();
    }

    @Override
    public EndpointAvailabilityManager addRequestHandler(List<String> methods, String path,
                                                         RequestHandlerProxy requestHandler) {
      return EndpointAvailabilityManager.forSdkApi(delegate.addRequestHandler(methods, path, (ctx, readyCallback) -> {
        // TODO: Null?
        requestHandler.handleRequest(RequestContext.forSdkApi(ctx), null);
      }));
    }

    @Override
    public EndpointAvailabilityManager addRequestHandler(String path, RequestHandlerProxy requestHandler) {
      return EndpointAvailabilityManager.forSdkApi(delegate.addRequestHandler(path, (ctx, readyCallback) -> {
        requestHandler.handleRequest(RequestContext.forSdkApi(ctx), HttpResponseReadyCallbackProxy.forSdkApi(readyCallback));
      }));
    }

    static void configureServerBuilderForSdkApi(HttpServerConfigurer configurer,
                                                String configName,
                                                HttpListenerProvider.ConnectionParams connectionParams,
                                                TlsContextFactory tlsContext,
                                                Supplier<Scheduler> ioSchedulerSupplier) {
      configurer.setHost(connectionParams.getHost())
          .setPort(connectionParams.getPort())
          .setTlsContextFactory(tlsContext)
          .setUsePersistentConnections(connectionParams.getUsePersistentConnections())
          .setConnectionIdleTimeout(connectionParams.getConnectionIdleTimeout())
          .setName(configName)
          .setSchedulerSupplier(ioSchedulerSupplier)
          .setReadTimeout(connectionParams.getReadTimeout());
    }
  }

  class HttpServerForMuleApi implements HttpServerProxy {

    private static final Logger LOGGER = getLogger(HttpServerForMuleApi.class);
    private static final String DEFAULT_READ_TIME_OUT_IN_MILLIS = "30000";

    private final org.mule.runtime.http.api.server.HttpServer delegate;

    public HttpServerForMuleApi(org.mule.runtime.http.api.server.HttpServer delegate) {
      this.delegate = delegate;
    }

    public void start() throws IOException {
      delegate.start();
    }

    public void stop() {
      delegate.stop();
    }

    public boolean isStopped() {
      return delegate.isStopped();
    }

    public boolean isStopping() {
      return delegate.isStopping();
    }

    public void dispose() {
      delegate.dispose();
    }

    public String getIp() {
      return delegate.getServerAddress().getIp();
    }

    public int getPort() {
      return delegate.getServerAddress().getPort();
    }

    public EndpointAvailabilityManager addRequestHandler(List<String> methods, String path,
                                                         RequestHandlerProxy requestHandler) {
      return EndpointAvailabilityManager.forMuleApi(delegate.addRequestHandler(methods, path, (ctx, readyCallback) -> {
        // TODO: Null?
        requestHandler.handleRequest(RequestContext.forMuleApi(ctx), null);
      }));
    }

    public EndpointAvailabilityManager addRequestHandler(String path, RequestHandlerProxy requestHandler) {
      return EndpointAvailabilityManager.forMuleApi(delegate.addRequestHandler(path, (ctx, readyCallback) -> {
        // TODO: Null?
        requestHandler.handleRequest(RequestContext.forMuleApi(ctx), null);
      }));
    }


    static HttpServerConfiguration getServerConfigurationForMuleApi(String configName,
                                                                    HttpListenerProvider.ConnectionParams connectionParams,
                                                                    TlsContextFactory tlsContext,
                                                                    Supplier<Scheduler> ioSchedulerSupplier) {
      HttpServerConfiguration.Builder builder = new HttpServerConfiguration.Builder()
          .setHost(connectionParams.getHost())
          .setPort(connectionParams.getPort())
          .setTlsContextFactory(tlsContext)
          .setUsePersistentConnections(connectionParams.getUsePersistentConnections())
          .setConnectionIdleTimeout(connectionParams.getConnectionIdleTimeout())
          .setName(configName)
          .setSchedulerSupplier(ioSchedulerSupplier);

      setReadTimeoutWithReflection(builder, connectionParams);

      return builder.build();
    }

    // This is done via reflection because it was added before forward compatibility.
    static void setReadTimeoutWithReflection(HttpServerConfiguration.Builder builder,
                                             HttpListenerProvider.ConnectionParams connectionParams) {
      Method method = getMethod(HttpServerConfiguration.Builder.class, "setReadTimeout", new Class[] {long.class});
      if (method != null) {
        try {
          method.invoke(builder, connectionParams.getReadTimeout());
        } catch (InvocationTargetException | IllegalAccessException e) {
          throw new MuleRuntimeException(createStaticMessage("Exception while calling method by reflection"), e);
        }
      } else if (connectionParams.getReadTimeout() != parseLong(DEFAULT_READ_TIME_OUT_IN_MILLIS)) {
        LOGGER
            .warn("The current Mule version does not support the configuration of the Read Timeout parameter, please update to the newest version");
      }
    }
  }
}
