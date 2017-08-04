/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;

/**
 * If the {@link org.mule.extension.http.internal.listener.HttpListener} needs to be notified about an error in the intecepting
 * process, then it should raise a child of {@link InterceptingException}.
 *
 * This object suggests the expected return headers and status code of the blocked request.
 */
public abstract class InterceptingException extends RuntimeException {

  private final HttpStatus status;
  private final MultiMap<String, String> headers;

  public InterceptingException(HttpStatus status, MultiMap<String, String> headers) {
    this.status = status;
    this.headers = headers;
  }

  /**
   * Suggested status code
   * 
   * @return status code
   */
  public HttpStatus status() {
    return status;
  }

  /**
   * Suggested response headers
   * 
   * @return response headers
   */
  public MultiMap<String, String> headers() {
    return headers;
  }
}
