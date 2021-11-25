/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.extension.http.internal.request.UriUtils;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;

public class HttpRequesterSimpleRequestBuilder {

  /**
   * The body of the response message
   */
  @Parameter
  @Optional
  @DisplayName("Body")
  private Literal<String> pollerBody;

  /**
   * HTTP headers the message should include.
   */
  @Parameter
  @Optional
  @NullSafe
  @DisplayName("Headers")
  protected MultiMap<String, String> pollerHeaders = emptyMultiMap();

  /**
   * URI parameters that should be used to create the request.
   */
  @Parameter
  @Optional
  @DisplayName("URI Parameters")
  private Map<String, String> pollerUriParams = emptyMap();

  /**
   * Query parameters the request should include.
   */
  @Parameter
  @Optional
  @DisplayName("Query Parameters")
  private MultiMap<String, String> pollerQueryParams = emptyMultiMap();

  public TypedValue<Object> getBody() {
    return TypedValue.of(pollerBody);
  }

  public void setBody(Literal<String> body) {
    this.pollerBody = body;
  }

  public MultiMap<String, String> getHeaders() {
    return pollerHeaders;
  }

  public void setHeaders(MultiMap<String, String> headers) {
    this.pollerHeaders = headers != null ? headers : emptyMultiMap();
  }

  public String replaceUriParams(String path) {
    return UriUtils.replaceUriParams(path, pollerUriParams);
  }

  public MultiMap<String, String> getQueryParams() {
    return pollerQueryParams.toImmutableMultiMap();
  }

  public Map<String, String> getUriParams() {
    return unmodifiableMap(pollerUriParams);
  }

  public void setQueryParams(MultiMap<String, String> queryParams) {
    this.pollerQueryParams = queryParams;
  }

  public void setUriParams(Map<String, String> uriParams) {
    this.pollerUriParams = uriParams;
  }

  public HttpRequestBuilder configure(HttpRequesterConfig config) {
    return HttpRequest.builder(PRESERVE_HEADER_CASE || config.isPreserveHeadersCase())
        .headers(getHeaders())
        .queryParams(getQueryParams());
  }
}
