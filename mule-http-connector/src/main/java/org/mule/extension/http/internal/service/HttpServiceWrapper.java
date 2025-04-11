/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service;

import static org.mule.extension.http.internal.service.server.HttpServerProxy.forMuleApi;
import static org.mule.extension.http.internal.service.server.HttpServerProxy.forSdkApi;

import org.mule.extension.http.internal.listener.HttpListenerProvider.ConnectionParams;
import org.mule.extension.http.internal.service.server.HttpServerProxy;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.http.api.server.ServerCreationException;

import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

public class HttpServiceWrapper {

  @Inject
  private org.mule.runtime.http.api.HttpService muleService;

  @Inject
  private Optional<org.mule.sdk.api.http.HttpService> sdkApiService;

  public HttpServerProxy createServer(String configName,
                                      ConnectionParams connectionParams,
                                      TlsContextFactory tlsContext,
                                      Supplier<Scheduler> ioSchedulerSupplier)
      throws ServerCreationException {
    if (sdkApiService.isPresent()) {
      return forSdkApi(sdkApiService.get(), configName, connectionParams, tlsContext, ioSchedulerSupplier);
    } else {
      return forMuleApi(muleService, configName, connectionParams, tlsContext, ioSchedulerSupplier);
    }
  }

}
