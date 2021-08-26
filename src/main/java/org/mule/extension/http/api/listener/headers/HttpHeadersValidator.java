/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import org.mule.runtime.api.util.MultiMap;

/**
 * Interface used to make validations on the headers from an HTTP request.
 *
 * @since 1.6.0
 */
public interface HttpHeadersValidator {

  /**
   * Makes some validation on the headers from an HTTP request.
   * @param headers Headers in the request.
   * @throws HttpHeadersException if an error related to headers is found.
   */
  void validateHeaders(MultiMap<String, String> headers) throws HttpHeadersException;
}
