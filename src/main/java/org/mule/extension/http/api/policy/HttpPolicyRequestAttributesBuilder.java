/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static java.util.Objects.requireNonNull;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;

import org.mule.runtime.api.util.MultiMap;

import java.util.Map;

/**
 * Builder for {@link HttpPolicyRequestAttributes}.
 *
 * @since 1.6
 */
public class HttpPolicyRequestAttributesBuilder {

  private MultiMap<String, String> headers = emptyMultiMap();
  private MultiMap<String, String> queryParams = emptyMultiMap();
  private Map<String, String> uriParams = emptyMultiMap();
  private String requestPath;

  public HttpPolicyRequestAttributesBuilder() {}

  public HttpPolicyRequestAttributesBuilder(HttpPolicyRequestAttributes attributes) {
    this.headers = attributes.getHeaders();
    this.queryParams = attributes.getQueryParams();
    this.uriParams = attributes.getUriParams();
    this.requestPath = attributes.getRequestPath();
  }

  public HttpPolicyRequestAttributesBuilder headers(MultiMap<String, String> headers) {
    requireNonNull(headers, "HTTP headers cannot be null.");
    this.headers = headers;
    return this;
  }

  public HttpPolicyRequestAttributesBuilder queryParams(MultiMap<String, String> queryParams) {
    requireNonNull(queryParams, "HTTP Query params cannot be null.");
    this.queryParams = queryParams;
    return this;
  }

  public HttpPolicyRequestAttributesBuilder uriParams(Map<String, String> uriParams) {
    requireNonNull(uriParams, "HTTP Uri params cannot be null.");
    this.uriParams = uriParams;
    return this;
  }

  public HttpPolicyRequestAttributesBuilder requestPath(String requestPath) {
    this.requestPath = requestPath;
    return this;
  }

  public HttpPolicyRequestAttributes build() {
    return new HttpPolicyRequestAttributes(headers, queryParams, uriParams, requestPath);
  }
}
