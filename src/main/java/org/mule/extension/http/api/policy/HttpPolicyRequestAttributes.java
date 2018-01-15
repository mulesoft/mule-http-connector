/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static java.lang.System.lineSeparator;

import org.mule.extension.http.api.BaseHttpRequestAttributes;
import org.mule.runtime.api.util.MultiMap;

import java.util.Map;

/**
 * {@link BaseHttpRequestAttributes} subclass that allows modification of request attributes and
 * creation through the expression language.
 *
 * @since 1.0
 */
public class HttpPolicyRequestAttributes extends BaseHttpRequestAttributes {

  private static final long serialVersionUID = 7856582596752161400L;

  public HttpPolicyRequestAttributes(MultiMap<String, String> headers, MultiMap<String, String> queryParams,
                                     Map<String, String> uriParams, String requestPath) {
    super(headers, queryParams, uriParams, requestPath);
  }

  public HttpPolicyRequestAttributes() {
    super(new MultiMap<>(), new MultiMap<>(), new MultiMap<>(), "");
  }

  public void setHeaders(MultiMap<String, String> headers) {
    this.headers = headers;
  }

  public void setQueryParams(MultiMap<String, String> queryParams) {
    this.queryParams = queryParams;
  }

  public void setUriParams(Map<String, String> uriParams) {
    this.uriParams = uriParams;
  }

  public String toString() {

    StringBuilder builder = new StringBuilder();
    String tab = "   ";
    String doubleTab = tab + tab;
    builder.append(this.getClass().getName()).append(lineSeparator()).append("{").append(lineSeparator())
        .append(tab).append("Request path=").append(requestPath).append(lineSeparator())
        .append(tab).append("Headers=[").append(lineSeparator());

    headers.entrySet().stream()
        .forEach(header -> builder.append(doubleTab).append(header.getKey()).append("=").append(header.getValue())
            .append(lineSeparator()));

    builder.append(tab).append("]");

    if (queryParams != null) {
      builder.append(lineSeparator()).append(tab).append("Query Parameters=[").append(lineSeparator());
      queryParams.entrySet().stream()
          .forEach(queryParam -> builder.append(doubleTab).append(queryParam.getKey()).append("=").append(queryParam.getValue())
              .append(lineSeparator()));
      builder.append(tab).append("]");
    }
    if (uriParams != null) {
      builder.append(lineSeparator()).append(tab).append("URI Parameters=[").append(lineSeparator());
      uriParams.entrySet().stream()
          .forEach(uriParam -> builder.append(doubleTab).append(uriParam.getKey()).append("=").append(uriParam.getValue())
              .append(lineSeparator()));
      builder.append(tab).append("]");
    }

    builder.append(lineSeparator());
    builder.append("}");

    return builder.toString();
  }
}
