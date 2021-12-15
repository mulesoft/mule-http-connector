/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;

/**
 * Component that specifies how to create a proper HTTP simple request, that can be used in a connectivity testing.
 *
 * @since 1.7
 */
public class HttpRequesterTestRequestBuilder {

  /**
   * The body in the connectivity test request. It can't be an expression because it doesn't make sense in a
   * connectivity testing context.
   */
  @Parameter
  @Optional(defaultValue = "")
  @Text
  @Expression(value = NOT_SUPPORTED)
  @DisplayName("Body")
  private String requestBody;

  /**
   * HTTP headers the connectivity test request should include. It allows multiple headers with the same key.
   */
  @Parameter
  @Optional
  @NullSafe
  @Expression(value = NOT_SUPPORTED)
  @DisplayName("Headers")
  private List<TestRequestHeader> requestHeaders = emptyList();

  /**
   * Query parameters the connectivity test request should include. It allows multiple query params with the same key.
   */
  @Parameter
  @Optional
  @NullSafe
  @Expression(value = NOT_SUPPORTED)
  @DisplayName("Query Parameters")
  private List<TestQueryParam> requestQueryParams = emptyList();

  /**
   * URI parameters the connectivity test request should include.
   */
  @Parameter
  @Optional
  @NullSafe
  @Expression(value = NOT_SUPPORTED)
  @DisplayName("URI Parameters")
  private List<UriParam> requestUriParams = emptyList();

  public List<TestRequestHeader> getRequestHeaders() {
    return unmodifiableList(requestHeaders);
  }

  public List<TestQueryParam> getRequestQueryParams() {
    return unmodifiableList(requestQueryParams);
  }

  public List<UriParam> getRequestUriParams() {
    return requestUriParams;
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

  public HttpEntity buildEntity() {
    if (requestBody == null || requestBody.isEmpty()) {
      return new EmptyHttpEntity();
    } else {
      return new InputStreamHttpEntity(new ByteArrayInputStream(requestBody.getBytes(UTF_8)));
    }
  }
}
