/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;

import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.util.List;

/**
 * Component that specifies how to create a proper HTTP simple request, that can be used in a Polling Source
 *
 * @since 1.8
 */
public class HttpRequesterSimpleRequestBuilder {

  /**
   * The body of the response message
   */
  @Parameter
  @Optional(defaultValue = "")
  @Text
  @DisplayName("Body")
  private Literal<String> pollingRequestBody;

  /**
   * HTTP headers the message should include.
   */
  @Parameter
  @Optional
  @NullSafe
  @DisplayName("Headers")
  protected List<SimpleRequestHeader> pollingRequestHeaders = emptyList();

  /**
   * URI parameters that should be used to create the request.
   */
  @Parameter
  @Optional
  @NullSafe
  @DisplayName("URI Parameters")
  private List<SimpleUriParam> pollingRequestUriParams = emptyList();

  /**
   * Query parameters the request should include.
   */
  @Parameter
  @Optional
  @NullSafe
  @DisplayName("Query Parameters")
  private List<SimpleQueryParam> pollingRequestQueryParams = emptyList();

  public String getPollingRequestBody() {
    return pollingRequestBody.getLiteralValue().orElse("");
  }

  public List<SimpleRequestHeader> getRequestHeaders() {
    return unmodifiableList(pollingRequestHeaders);
  }

  public List<SimpleQueryParam> getRequestQueryParams() {
    return unmodifiableList(pollingRequestQueryParams);
  }

  public List<SimpleUriParam> getRequestUriParams() {
    return unmodifiableList(pollingRequestUriParams);
  }

  public HttpRequestBuilder toHttpRequestBuilder(HttpRequesterConfig config) {
    return HttpRequest.builder(PRESERVE_HEADER_CASE || config.isPreserveHeadersCase());
  }

}
