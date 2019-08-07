/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.server;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mule.extension.http.api.listener.server.RequestAddressesFormatValueProvider.ADDRESS_ONLY;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

import org.mule.extension.http.api.listener.intercepting.cors.CorsInterceptorWrapper;
import org.mule.extension.http.internal.listener.address.AddressOnlyFormat;
import org.mule.extension.http.internal.listener.address.HostnameAndAddressFormat;
import org.mule.extension.http.internal.listener.HttpListener;
import org.mule.extension.http.internal.listener.HttpListenerProvider;
import org.mule.extension.http.internal.listener.ListenerPath;
import org.mule.extension.http.internal.listener.intercepting.HttpListenerInterceptor;
import org.mule.extension.http.internal.listener.address.RequestAddressesFormat;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.values.OfValues;

/**
 * Configuration element for a {@link HttpListener}.
 *
 * @since 1.0
 */
@Configuration(name = "listenerConfig")
@ConnectionProviders(HttpListenerProvider.class)
@Sources(HttpListener.class)
public class HttpListenerConfig implements Initialisable {

  /**
   * Base path to use for all requests that reference this config.
   */
  @Parameter
  @Optional
  @Expression(NOT_SUPPORTED)
  private String basePath;

  /**
   * Listener interceptors that will apply on request and on response events.
   */
  @Parameter
  @Optional
  @Expression(NOT_SUPPORTED)
  private CorsInterceptorWrapper listenerInterceptors;

  /**
   * Addresses format used to populate local and remote address property in the HTTP request properties
   */
  @Parameter
  @Optional(defaultValue = ADDRESS_ONLY)
  @Expression(NOT_SUPPORTED)
  @Placement(order = 1, tab = ADVANCED_TAB)
  @OfValues(value = RequestAddressesFormatValueProvider.class)
  private String requestAddressesFormat;

  @Override
  public void initialise() {
    basePath = sanitizePathWithStartSlash(this.basePath);
  }

  public ListenerPath getFullListenerPath(String listenerPath) {
    checkArgument(listenerPath.startsWith("/"), "listenerPath must start with /");
    return new ListenerPath(basePath, listenerPath);
  }

  public String sanitizePathWithStartSlash(String path) {
    if (path == null) {
      return null;
    }
    return path.startsWith("/") ? path : "/" + path;
  }

  public java.util.Optional<HttpListenerInterceptor> getInterceptor() {
    return listenerInterceptors != null ? of(listenerInterceptors.getInterceptor()) : empty();
  }

  public RequestAddressesFormat getRequestAddressesFormat() {
    return requestAddressesFormat.equals(ADDRESS_ONLY) ? new AddressOnlyFormat() : new HostnameAndAddressFormat();
  }
}
