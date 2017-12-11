/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.extension.api.runtime.parameter.OutboundCorrelationStrategy.AUTO;
import org.mule.extension.http.api.request.builder.QueryParam;
import org.mule.extension.http.api.request.builder.RequestHeader;
import org.mule.extension.http.internal.HttpStreamingType;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.parameter.OutboundCorrelationStrategy;

import java.util.List;

/**
 * Groups parameters which configure how a request is done
 *
 * @since 1.0
 */
public final class RequestSettings {

  /**
   * Specifies whether to follow redirects or not. Default value is true.
   */
  @Parameter
  @Optional(defaultValue = "true")
  private boolean followRedirects;

  /**
   * Defines if the request should contain a body or not. If AUTO, it will depend on the method (GET, HEAD and OPTIONS will not
   * send a body).
   */
  @Parameter
  @Optional(defaultValue = "AUTO")
  private HttpSendBodyMode sendBodyMode;

  /**
   * Defines if the request should be sent using streaming or not. If this attribute is not present, the behavior will depend on
   * the type of the payload (it will stream only for InputStream). If set to true, it will always stream. If set to false, it
   * will never stream. As streaming is done the request will be sent user Transfer-Encoding: chunked.
   */
  @Parameter
  @Optional(defaultValue = "AUTO")
  @Summary("Defines if the request should be sent using streaming or not. If this attribute is not present, "
      + "the behavior will depend on the type of the payload (it will stream only for InputStream).")
  private HttpStreamingType requestStreamingMode;

  /**       
   * If true, cookies received in HTTP responses will be stored, and sent in subsequent HTTP requests.        
   */
  @Parameter
  @Optional(defaultValue = "true")
  @Expression(NOT_SUPPORTED)
  private boolean enableCookies;

  /**
   * Default HTTP headers the message should include.
   */
  @Parameter
  @Optional
  @NullSafe
  private List<RequestHeader> defaultHeaders;

  /**
   * Default Query parameters the request should include.
   */
  @Parameter
  @Optional
  @NullSafe
  @DisplayName("Query Parameters")
  private List<QueryParam> defaultQueryParams;

  /**
   * Whether to specify a correlationId when publishing messages. This applies both for custom correlation ids specifies at the
   * operation level and for default correlation Ids taken from the current event
   */
  @Parameter
  @Optional(defaultValue = "AUTO")
  private OutboundCorrelationStrategy sendCorrelationId = AUTO;

  public List<RequestHeader> getDefaultHeaders() {
    return defaultHeaders;
  }

  public List<QueryParam> getDefaultQueryParams() {
    return defaultQueryParams;
  }

  public boolean getFollowRedirects() {
    return followRedirects;
  }

  public HttpSendBodyMode getSendBodyMode() {
    return sendBodyMode;
  }

  public HttpStreamingType getRequestStreamingMode() {
    return requestStreamingMode;
  }

  public boolean isEnableCookies() {
    return enableCookies;
  }

  public OutboundCorrelationStrategy getSendCorrelationId() {
    return sendCorrelationId;
  }
}
