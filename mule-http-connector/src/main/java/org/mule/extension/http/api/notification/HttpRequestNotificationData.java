/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.notification;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

/**
 * HTTP notification data regarding requests.
 *
 * @since 1.1
 */
public class HttpRequestNotificationData extends HttpNotificationData {

  private final String method;
  private final String uri;
  private final MultiMap<String, String> queryParams;

  /**
   * Creates an {@link HttpRequestNotificationData} based on an {@link HttpRequest}
   *
   * @param request {@link HttpRequest} to use as template
   * @return an equivalent {@link HttpRequestNotificationData}
   */
  public static HttpRequestNotificationData from(HttpRequest request) {
    return new HttpRequestNotificationData(request);
  }

  HttpRequestNotificationData(HttpRequest request) {
    super(request);
    this.method = request.getMethod();
    this.uri = request.getUri().toString();
    this.queryParams = request.getQueryParams().toImmutableMultiMap();
  }

  public String getMethod() {
    return method;
  }

  public String getUri() {
    return uri;
  }

  public MultiMap<String, String> getQueryParams() {
    return queryParams;
  }

}
