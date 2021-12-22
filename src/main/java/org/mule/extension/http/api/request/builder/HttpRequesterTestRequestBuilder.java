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
import static org.mule.extension.http.internal.request.KeyValuePairUtils.toMultiMap;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.api.meta.ExpressionSupport.SUPPORTED;
import static org.mule.runtime.api.metadata.DataType.STRING;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;

import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;

/**
 * Component that specifies how to create a proper HTTP simple request, that can be used in a connectivity testing.
 *
 * @since 1.7
 */
public class HttpRequesterTestRequestBuilder implements Initialisable, HttpRequestBuilderConfigurer {

  /**
   * The body in the connectivity test request. It can be an expression, but it won't have any binding.
   */
  @Parameter
  @Optional
  @Text
  @Expression(value = SUPPORTED)
  @DisplayName("Body")
  private Literal<String> requestBody;

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

  @Inject
  private ExpressionManager expressionManager;

  // The request body can be an expression, but as it hasn't any binding we can cache the result in this variable.
  private String resolvedRequestBody;

  public List<TestRequestHeader> getRequestHeaders() {
    return unmodifiableList(requestHeaders);
  }

  public List<TestQueryParam> getRequestQueryParams() {
    return unmodifiableList(requestQueryParams);
  }

  public List<UriParam> getRequestUriParams() {
    return requestUriParams;
  }

  protected void setRequestQueryParams(List<TestQueryParam> queryParams) {
    this.requestQueryParams = queryParams;
  }

  @Override
  public HttpRequestBuilder toHttpRequestBuilder(HttpRequesterConfig config) {
    return HttpRequest.builder(PRESERVE_HEADER_CASE || config.isPreserveHeadersCase())
        .headers(toMultiMap(getRequestHeaders()))
        .queryParams(toMultiMap(getRequestQueryParams()));
  }

  @Override
  public TypedValue getBodyAsTypedValue() {
    return TypedValue.of(resolveRequestBody());
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

  @Override
  public void initialise() throws InitialisationException {
    this.resolvedRequestBody = resolveRequestBody();
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
