/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static java.lang.System.lineSeparator;

import org.mule.runtime.api.util.MultiMap;

import java.io.Serializable;

/**
 * Representation of an HTTP response message attributes that can be created through
 * the expression language to modify the response parameters of the http:listener
 * using policies.
 *
 * @since 1.0
 */
public class HttpPolicyResponseAttributes implements Serializable {

  protected static final String tab = "   ";
  protected static final String doubleTab = tab + tab;

  private static final long serialVersionUID = 2530600012948674328L;

  /**
   * HTTP status code of the response. Former 'http.status'.
   */
  private Integer statusCode;
  /**
   * HTTP reason phrase of the response. Former 'http.reason'.
   */
  private String reasonPhrase;

  /**
   * Map of HTTP headers in the message. Former properties.
   */
  private MultiMap<String, String> headers = new MultiMap<>();

  public Integer getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public void setReasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
  }

  public MultiMap<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(MultiMap<String, String> headers) {
    this.headers = headers;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(this.getClass().getName())
        .append(lineSeparator()).append("{")
        .append(lineSeparator()).append(tab).append("Status Code=").append(statusCode)
        .append(lineSeparator()).append(tab).append("Reason Phrase=").append(reasonPhrase)
        .append(lineSeparator()).append(tab).append("Headers=[");

    if (headers.isEmpty()) {
      builder.append("]");
    } else {
      headers.entryList().stream()
          .forEach(header -> builder.append(lineSeparator()).append(doubleTab)
              .append(header.getKey()).append("=").append(header.getValue()));

      builder.append(lineSeparator()).append(tab).append("]");
    }
    builder.append(lineSeparator()).append("}");

    return builder.toString();
  }
}
