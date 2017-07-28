/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting;

import org.mule.runtime.http.api.HttpConstants;

import java.util.Map;

public abstract class InterceptorException extends RuntimeException {

  private final HttpConstants.HttpStatus status;
  private final Map<String, String> headers;

  public InterceptorException(HttpConstants.HttpStatus status, Map<String, String> headers) {
    this.status = status;
    this.headers = headers;
  }

  public HttpConstants.HttpStatus status() {
    return status;
  }

  public Map<String, String> headers() {
    return headers;
  }
}
