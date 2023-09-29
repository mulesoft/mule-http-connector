/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.intercepting.cors;

import org.mule.extension.http.internal.listener.intercepting.HttpListenerInterceptor;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.Objects;

/**
 * The HTTP Connector MAY allow (in the future) a collection of interceptors in the listener. If this is implemented, we would
 * like to have the XML config structure backward compatible.
 * <p>
 * {@link CorsInterceptorWrapper} emulates the XML structure within the listener config for handling multiple interceptors but
 * restricted only to CORS.
 */
public class CorsInterceptorWrapper {

  /**
   * Interceptor which validates that requests match CORS specification and acts on responses accordingly.
   */
  @Parameter
  private CorsListenerInterceptor corsInterceptor;

  public HttpListenerInterceptor getInterceptor() {
    return corsInterceptor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CorsInterceptorWrapper that = (CorsInterceptorWrapper) o;
    return Objects.equals(corsInterceptor, that.corsInterceptor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(corsInterceptor);
  }
}
