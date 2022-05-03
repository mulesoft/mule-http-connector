/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.hash;
import static org.mule.runtime.api.metadata.DataType.STRING;

import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
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

  private final Literal<String> requestBody;
  private final List<TestRequestHeader> requestHeaders;
  private final List<TestQueryParam> requestQueryParams;
  private final List<UriParam> requestUriParams;
  private final ExpressionManager expressionManager;

  // The request body can be an expression, but as it hasn't any binding we can cache the result in this variable.
  private final String resolvedRequestBody;

  public HttpRequesterTestRequestBuilder(Literal<String> requestBody,
                                         List<TestRequestHeader> requestHeaders,
                                         List<TestQueryParam> requestQueryParams,
                                         List<UriParam> requestUriParams,
                                         ExpressionManager expressionManager) {
    this.requestBody = requestBody;
    this.requestHeaders = requestHeaders;
    this.requestQueryParams = requestQueryParams;
    this.requestUriParams = requestUriParams;
    this.expressionManager = expressionManager;
    this.resolvedRequestBody = resolveRequestBody();
  }

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
    if (resolvedRequestBody == null || resolvedRequestBody.isEmpty()) {
      return new EmptyHttpEntity();
    } else {
      return new InputStreamHttpEntity(new ByteArrayInputStream(resolvedRequestBody.getBytes(UTF_8)));
    }
  }

  private String resolveRequestBody() {
    if (requestBody == null) {
      // The request body isn't present.
      return null;
    }

    java.util.Optional<String> optRequestBody = requestBody.getLiteralValue();
    if (!optRequestBody.isPresent()) {
      // The request body isn't present.
      return null;
    }

    String requestBodyValue = optRequestBody.get();
    if (isExpression(requestBodyValue)) {
      // Resolve the expression.
      return (String) expressionManager.evaluate(requestBodyValue, STRING).getValue();
    } else {
      // Just return the Literal value.
      return requestBodyValue;
    }
  }

  private static boolean isExpression(String value) {
    String trim = value.trim();
    return trim.startsWith("#[") && trim.endsWith("]");
  }
}
