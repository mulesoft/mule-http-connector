/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;
import static org.mule.extension.http.internal.request.KeyValuePairUtils.toMultiMap;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;

import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.extension.http.internal.request.UriUtils;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Component that specifies how to create a proper HTTP simple request, that can be used in a connectivity testing.
 *
 * @since 1.7
 */
public class HttpRequesterTestRequestBuilder implements HttpRequestBuilderConfigurer {

  /**
   * The body of the response message
   */
  @Parameter
  @Optional(defaultValue = "")
  @Text
  @Expression(value = NOT_SUPPORTED)
  @DisplayName("Body")
  private String requestBody;

  /**
   * HTTP headers the message should include.
   */
  @Parameter
  @Optional
  @NullSafe
  @Expression(value = NOT_SUPPORTED)
  @DisplayName("Headers")
  protected List<TestRequestHeader> requestHeaders = emptyList();

  /**
   * URI parameters that should be used to create the request.
   */
  @Parameter
  @Optional
  @NullSafe
  @Expression(value = NOT_SUPPORTED)
  @DisplayName("URI Parameters")
  private Map<String, String> requestUriParams = emptyMap();

  /**
   * Query parameters the request should include.
   */
  @Parameter
  @Optional
  @NullSafe
  @Expression(value = NOT_SUPPORTED)
  @DisplayName("Query Parameters")
  private List<TestQueryParam> requestQueryParams = emptyList();

  public String getRequestBody() {
    return requestBody;
  }

  protected void setRequestBody(String body) {
    this.requestBody = body;
  }

  public List<TestRequestHeader> getRequestHeaders() {
    return unmodifiableList(requestHeaders);
  }

  protected void setRequestHeaders(List<TestRequestHeader> headers) {
    this.requestHeaders = headers != null ? headers : emptyList();
  }

  public String replaceUriParamsOf(String path) {
    return UriUtils.replaceUriParams(path, requestUriParams);
  }

  public List<TestQueryParam> getRequestQueryParams() {
    return unmodifiableList(requestQueryParams);
  }

  public Map<String, String> getRequestUriParams() {
    return unmodifiableMap(requestUriParams);
  }

  protected void setRequestQueryParams(List<TestQueryParam> queryParams) {
    this.requestQueryParams = queryParams;
  }

  protected void setRequestUriParams(Map<String, String> uriParams) {
    this.requestUriParams = uriParams;
  }

  @Override
  public HttpRequestBuilder toHttpRequestBuilder(HttpRequesterConfig config) {
    return HttpRequest.builder(PRESERVE_HEADER_CASE || config.isPreserveHeadersCase())
        .headers(toMultiMap(getRequestHeaders()))
        .queryParams(toMultiMap(getRequestQueryParams()));
  }

  @Override
  public TypedValue<?> getBodyAsTypedValue() {
    return TypedValue.of(getRequestBody());
  }

  @Override
  public int hashCode() {
    return hash(requestBody, requestHeaders, requestQueryParams, requestUriParams);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HttpRequesterTestRequestBuilder that = (HttpRequesterTestRequestBuilder) o;
    return Objects.equals(requestBody, that.requestBody) && Objects.equals(requestHeaders, that.requestHeaders)
        && Objects.equals(requestQueryParams, that.requestQueryParams) && Objects.equals(requestUriParams, that.requestUriParams);
  }
}
