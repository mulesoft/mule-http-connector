/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.notification;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.net.URI;

/**
 * HTTP notification data regarding requests.
 *
 * @since 1.1
 */
public class HttpRequestData extends HttpData {

  private final String method;
  private final URI uri;
  private final MultiMap<String, String> queryParams;

  public static HttpRequestData from(HttpRequest request) {
    return new HttpRequestData(request);
  }

  HttpRequestData(HttpRequest request) {
    super(request);
    this.method = request.getMethod();
    this.uri = request.getUri();
    this.queryParams = request.getQueryParams().toImmutableMultiMap();
  }

  public String getMethod() {
    return method;
  }

  public URI getUri() {
    return uri;
  }

  public MultiMap<String, String> getQueryParams() {
    return queryParams;
  }

}
