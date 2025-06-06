/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.notification;

import org.mule.sdk.api.http.domain.message.response.HttpResponse;

/**
 * HTTP notification data regarding responses.
 *
 * @since 1.1
 */
public class HttpResponseNotificationData extends HttpNotificationData {

  private final int statusCode;
  private final String reasonPhrase;

  /**
   * Creates an {@link HttpResponseNotificationData} based on an {@link HttpResponse}
   *
   * @param response {@link HttpResponse} to use as template
   * @return an equivalent {@link HttpResponseNotificationData}
   */
  public static HttpResponseNotificationData from(HttpResponse response) {
    return new HttpResponseNotificationData(response);
  }

  HttpResponseNotificationData(HttpResponse response) {
    super(response);
    this.statusCode = response.getStatusCode();
    this.reasonPhrase = response.getReasonPhrase();
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

}
