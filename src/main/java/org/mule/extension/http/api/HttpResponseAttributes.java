/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.lang.System.lineSeparator;

import org.mule.runtime.api.util.MultiMap;

/**
 * Representation of an HTTP response message attributes.
 *
 * @since 1.0
 */
public class HttpResponseAttributes extends HttpAttributes {

  private static final long serialVersionUID = -3131769059554988414L;

  /**
   * HTTP status code of the response. Former 'http.status'.
   */
  private final int statusCode;
  /**
   * HTTP reason phrase of the response. Former 'http.reason'.
   */
  private final String reasonPhrase;

  public HttpResponseAttributes(int statusCode, String reasonPhrase, MultiMap<String, String> headers) {
    super(headers);
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    String tab = "   ";
    String doubleTab = tab + tab;

    builder.append(this.getClass().getName()).append(lineSeparator()).append("{").append(lineSeparator())
        .append(tab).append("Status Code=").append(statusCode).append(lineSeparator())
        .append(tab).append("Reason Phrase=").append(reasonPhrase).append(lineSeparator())
        .append(tab).append("Headers=[").append(lineSeparator());

    headers.entrySet().stream()
        .forEach(header -> builder.append(doubleTab).append(header.getKey()).append("=").append(header.getValue())
            .append(lineSeparator()));

    builder.append(tab).append("]").append(lineSeparator()).append("}");

    return builder.toString();
  }
}
