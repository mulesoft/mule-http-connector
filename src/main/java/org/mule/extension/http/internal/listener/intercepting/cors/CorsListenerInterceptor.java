/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting.cors;

import org.mule.extension.http.internal.listener.intercepting.HttpListenerInterceptor;
import org.mule.extension.http.internal.listener.intercepting.Interception;
import org.mule.modules.cors.CorsKernel;
import org.mule.modules.cors.response.CorsAction;
import org.mule.runtime.api.util.MultiMap;

import java.util.ArrayList;

/**
 * CORS interceptor for {@link org.mule.extension.http.internal.listener.HttpListener}
 */
public class CorsListenerInterceptor extends CorsKernel implements HttpListenerInterceptor {

  public CorsListenerInterceptor() {
    super(new ArrayList<>());
  }

  /**
   * Validates that request matches CORS specification, and creates an {@link Interception} result.
   * 
   * @param method request's method.
   * @param headers request's headers.
   * @return the suggested {@link Interception} result.
   */
  @Override
  public Interception request(String method, MultiMap<String, String> headers) {
    CorsAction action = validate(method, headers);
    return interception().from(action);
  }

  private CorsInterceptionFactory interception() {
    return new CorsInterceptionFactory();
  }
}
