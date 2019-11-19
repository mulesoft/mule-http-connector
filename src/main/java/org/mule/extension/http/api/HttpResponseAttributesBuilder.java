/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.util.Objects.requireNonNull;
import static org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap.emptyCaseInsensitiveMultiMap;

import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;

/**
 * Builder for {@link HttpResponseAttributes}.
 *
 * @since 1.6
 */
public class HttpResponseAttributesBuilder {

  private CaseInsensitiveMultiMap headers = emptyCaseInsensitiveMultiMap();
  private int statusCode;
  private String reasonPhrase;

  public HttpResponseAttributesBuilder() {}

  public HttpResponseAttributesBuilder(HttpResponseAttributes attributes) {
    this.headers = attributes.getHeaders();
    this.statusCode = attributes.getStatusCode();
    this.reasonPhrase = attributes.getReasonPhrase();
  }

  public HttpResponseAttributesBuilder headers(CaseInsensitiveMultiMap headers) {
    requireNonNull(headers, "HTTP headers cannot be null.");
    this.headers = headers;
    return this;
  }

  public HttpResponseAttributesBuilder statusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  public HttpResponseAttributesBuilder reasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
    return this;
  }

  public HttpResponseAttributes build() {
    return new HttpResponseAttributes(statusCode, reasonPhrase, headers);
  }
}
