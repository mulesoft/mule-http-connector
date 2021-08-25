/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import org.mule.runtime.api.util.MultiMap;

public class IdempotentHeaderFilter implements HttpHeadersFilter {

  @Override
  public MultiMap<String, String> filter(MultiMap<String, String> headers) throws HttpHeaderError {
    return headers;
  }
}