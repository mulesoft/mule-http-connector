/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.util.ClassUtils.getMethod;

import static java.lang.Long.parseLong;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.internal.listener.HttpListenerProvider.ConnectionParams;
import org.mule.extension.http.internal.service.server.HttpServerProxy;
import org.mule.extension.http.internal.service.server.HttpServerProxyMuleApi;
import org.mule.extension.http.internal.service.server.HttpServerProxySdkApi;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.http.api.server.HttpServerConfiguration;
import org.mule.runtime.http.api.server.ServerCreationException;
import org.mule.sdk.api.http.server.HttpServerConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.slf4j.Logger;

public class HttpServiceProxy {

  private static final Logger LOGGER = getLogger(HttpServiceProxy.class);

  private static final String DEFAULT_READ_TIME_OUT_IN_MILLIS = "30000";

  @Inject
  org.mule.runtime.http.api.HttpService muleService;

  @Inject
  private Optional<org.mule.sdk.api.http.HttpService> sdkApiService;

  public HttpServerProxy createServer(String configName,
                                      ConnectionParams connectionParams,
                                      TlsContextFactory tlsContext,
                                      Supplier<Scheduler> ioSchedulerSupplier)
      throws ServerCreationException {
    if (sdkApiService.isPresent()) {
      return new HttpServerProxySdkApi(sdkApiService.get().server(builder -> configureServerBuilderForSdkApi(builder, configName, connectionParams, tlsContext, ioSchedulerSupplier)));
    }
    return new HttpServerProxyMuleApi(muleService.getServerFactory().create(getServerConfigurationForMuleApi(configName, connectionParams, tlsContext, ioSchedulerSupplier)));
  }

  private void configureServerBuilderForSdkApi(HttpServerConfigurationBuilder builder,
                                               String configName,
                                               ConnectionParams connectionParams,
                                               TlsContextFactory tlsContext,
                                               Supplier<Scheduler> ioSchedulerSupplier) {
    builder.setHost(connectionParams.getHost())
            .setPort(connectionParams.getPort())
            .setTlsContextFactory(tlsContext)
            .setUsePersistentConnections(connectionParams.getUsePersistentConnections())
            .setConnectionIdleTimeout(connectionParams.getConnectionIdleTimeout())
            .setName(configName)
            .setSchedulerSupplier(ioSchedulerSupplier)
            .setReadTimeout(connectionParams.getReadTimeout());
  }

  private HttpServerConfiguration getServerConfigurationForMuleApi(String configName, ConnectionParams connectionParams,
                                                                   TlsContextFactory tlsContext, Supplier<Scheduler> ioSchedulerSupplier) {
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

  private void setReadTimeoutWithReflection(HttpServerConfiguration.Builder builder, ConnectionParams connectionParams) {
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
