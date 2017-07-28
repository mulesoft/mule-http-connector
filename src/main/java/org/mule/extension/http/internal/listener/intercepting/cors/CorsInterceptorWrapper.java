/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting.cors;

import org.mule.extension.http.internal.listener.intercepting.HttpListenerInterceptor;
import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * The HTTP Connector MAY allow (in the future) a collection of interceptors in the listener. If this is implemented, we would
 * like to have the XML config structure backward compatible.
 * <p>
 * {@link CorsInterceptorWrapper} emulates the XML structure within the ListenerConfig for handling multiple interceptors but
 * restricted only to CORS.
 */
public class CorsInterceptorWrapper {

  @Parameter
  private CorsListenerInterceptor corsInterceptor;

  public HttpListenerInterceptor getInterceptor() {
    return corsInterceptor;
  }
}
