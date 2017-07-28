/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting.cors;

import org.mule.extension.http.internal.listener.intercepting.Interception;
import org.mule.modules.cors.response.AddCorsHeaders;
import org.mule.runtime.http.api.HttpHeaders;

import java.util.Map;

public class AddHeadersInterception implements Interception {

  private final AddCorsHeaders addCorsHeaders;

  public AddHeadersInterception(AddCorsHeaders addCorsHeaders) {
    this.addCorsHeaders = addCorsHeaders;
  }

  @Override
  public Map<String, String> getHeaders() {
    Map<String, String> headers = addCorsHeaders.headers();
    headers.put(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, addCorsHeaders.origin());
    return headers;
  }
}
