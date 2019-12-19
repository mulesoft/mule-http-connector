/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.lang.System.lineSeparator;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * Representation of an HTTP response message attributes.
 *
 * @since 1.0
 */
public class HttpResponseAttributes extends HttpAttributes {

  private static final long serialVersionUID = -3131769059554988414L;

  public static HttpResponseAttributesBuilder builder() {
    return new HttpResponseAttributesBuilder();
  }

  /**
   * HTTP status code of the response. Former 'http.status'.
   */
  @Parameter
  private final int statusCode;

  /**
   * HTTP reason phrase of the response. Former 'http.reason'.
   */
  @Parameter
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(this.getClass().getName()).append(lineSeparator()).append("{").append(lineSeparator())
        .append(TAB).append("Status Code=").append(statusCode).append(lineSeparator())
        .append(TAB).append("Reason Phrase=").append(reasonPhrase).append(lineSeparator());

    buildMapToString(headers, "Headers", headers.entryList().stream(), builder);

    builder.append("}");

    return builder.toString();
  }
}
