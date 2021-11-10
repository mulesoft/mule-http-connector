/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting.cors;

import static org.mule.runtime.http.api.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;

import org.mule.extension.http.api.listener.intercepting.Interception;
import org.mule.modules.cors.response.AddCorsHeaders;
import org.mule.runtime.api.util.MultiMap;

public class AddHeadersInterception implements Interception {

  private final MultiMap<String, String> headers;

  public AddHeadersInterception(AddCorsHeaders addCorsHeaders) {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.putAll(addCorsHeaders.headers());
    headers.put(ACCESS_CONTROL_ALLOW_ORIGIN, addCorsHeaders.origin());

    this.headers = headers.toImmutableMultiMap();
  }

  @Override
  public MultiMap<String, String> getHeaders() {
    return headers;
  }
}
