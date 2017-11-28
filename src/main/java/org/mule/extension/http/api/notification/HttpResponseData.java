/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.notification;

import org.mule.runtime.http.api.domain.message.response.HttpResponse;

/**
 * HTTP notification data regarding responses.
 *
 * @since 1.1
 */
public class HttpResponseData extends HttpData {

  private final int statusCode;
  private final String reasonPhrase;

  public static HttpResponseData from(HttpResponse response) {
    return new HttpResponseData(response);
  }

  HttpResponseData(HttpResponse response) {
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
