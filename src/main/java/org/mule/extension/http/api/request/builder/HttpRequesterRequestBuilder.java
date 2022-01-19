/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.extension.api.runtime.parameter.OutboundCorrelationStrategy.AUTO;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;

import org.mule.extension.http.api.HttpMessageBuilder;
import org.mule.extension.http.internal.request.CorrelationData;
import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.extension.http.internal.request.UriUtils;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.OutboundCorrelationStrategy;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.util.Map;

/**
 * Component that specifies how to create a proper HTTP request.
 *
 * @since 1.0
 */
public class HttpRequesterRequestBuilder extends HttpMessageBuilder {

  /**
   * The body of the response message
   */
  @Parameter
  @Content(primary = true)
  private TypedValue<Object> body;

  /**
   * HTTP headers the message should include.
   */
  @Parameter
  @Optional
  @Content
  @NullSafe
  protected MultiMap<String, String> headers = emptyMultiMap();

  /**
   * URI parameters that should be used to create the request.
   */
  @Parameter
  @Optional
  @Content
  @DisplayName("URI Parameters")
  private Map<String, String> uriParams = emptyMap();

  /**
   * Query parameters the request should include.
   */
  @Parameter
  @Optional
  @Content
  @DisplayName("Query Parameters")
  private MultiMap<String, String> queryParams = emptyMultiMap();

  /**
   * Options on whether to include an outbound correlation id or not
   */
  @Parameter
  @ConfigOverride
  private OutboundCorrelationStrategy sendCorrelationId = AUTO;

  /**
   * Allows to set a custom correlation id
   */
  @Parameter
  @Optional
  private String correlationId;

  private CorrelationInfo correlationInfo;

  @Override
  public TypedValue<Object> getBody() {
    return body;
  }

  @Override
  public void setBody(TypedValue<Object> body) {
    this.body = body;
  }

  @Override
  public MultiMap<String, String> getHeaders() {
    return headers;
  }

  @Override
  public void setHeaders(MultiMap<String, String> headers) {
    this.headers = headers != null ? headers : emptyMultiMap();
  }

  public String replaceUriParams(String path) {
    return UriUtils.replaceUriParams(path, uriParams);
  }

  public MultiMap<String, String> getQueryParams() {
    return queryParams.toImmutableMultiMap();
  }

  public Map<String, String> getUriParams() {
    return unmodifiableMap(uriParams);
  }

  public void setQueryParams(MultiMap<String, String> queryParams) {
    this.queryParams = queryParams;
  }

  public void setUriParams(Map<String, String> uriParams) {
    this.uriParams = uriParams;
  }

  public void setCorrelationInfo(CorrelationInfo correlationInfo) {
    this.correlationInfo = correlationInfo;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public CorrelationInfo getCorrelationInfo() {
    return correlationInfo;
  }

  public OutboundCorrelationStrategy getSendCorrelationId() {
    return sendCorrelationId;
  }

  /**
   * @deprecated
   * use {@link HttpRequesterRequestBuilder#toHttpRequestBuilder} instead
   */
  @Deprecated
  public HttpRequestBuilder configure(HttpRequesterConfig config) {
    return toHttpRequestBuilder(config);
  }

  public HttpRequestBuilder toHttpRequestBuilder(HttpRequesterConfig config) {
    return HttpRequest.builder(PRESERVE_HEADER_CASE || config.isPreserveHeadersCase())
        .headers(headers)
        .queryParams(queryParams);
  }

}
