/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.builder;

import static java.util.Objects.requireNonNull;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

/**
 * Creates {@link HttpResponseAttributes} based on an {@HttpResponse} and it's parts.
 */
public class HttpResponseAttributesBuilder {

  private MultiMap<String, String> headers = emptyMultiMap();
  private int statusCode;
  private String reasonPhrase;

  public HttpResponseAttributesBuilder setResponse(HttpResponse response) {
    this.headers = response.getHeaders();
    this.statusCode = response.getStatusCode();
    this.reasonPhrase = response.getReasonPhrase();

    return this;
  }

  public HttpResponseAttributesBuilder headers(MultiMap<String, String> headers) {
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
