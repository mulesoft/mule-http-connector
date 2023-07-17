/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import org.mule.runtime.http.api.HttpConstants.HttpStatus;

/**
 * Exception thrown by {@link HttpHeadersValidator} when a headers error is found.
 *
 * @since 1.6.0
 */
public class HttpHeadersException extends Exception {

  private static final long serialVersionUID = 4197611177382501828L;

  private final String errorMessage;
  private final HttpStatus statusCode;

  HttpHeadersException(final String errorMessage, final HttpStatus statusCode) {
    this.errorMessage = errorMessage;
    this.statusCode = statusCode;
  }

  public HttpStatus getStatusCode() {
    return statusCode;
  }

  @Override
  public String getMessage() {
    return errorMessage;
  }
}
