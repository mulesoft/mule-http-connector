/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting;

import org.mule.runtime.api.util.MultiMap;

/**
 * Defines the protocol for intercepting HTTP requests processed by the
 * {@link org.mule.extension.http.internal.listener.HttpListener}
 */
public interface HttpListenerInterceptor {

  /**
   * Intercepts the request's method and headers and returns an {@link Interception} as result. This can be used to decorate the
   * message or abort the execution of the flow.
   * 
   * @param method request's method
   * @param headers request's headers
   * @return an {@link Interception}
   */
  Interception request(String method, MultiMap<String, String> headers);
}
