/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.extension.api.runtime.parameter.OutboundCorrelationStrategy.AUTO;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.builder.QueryParam;
import org.mule.extension.http.api.request.builder.RequestHeader;
import org.mule.extension.http.api.streaming.HttpStreamingType;
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

  /**
   * By default, header keys are stored internally in lower-case. This is to improve performance of headers handling and is
   * functionally correct as specified in the RFC.
   * <p>
   * In the case a server expects headers in a specific case, this flag may be set to {@code true} so the case of the header keys
   * are preserved.
   */
  @Parameter
  @Optional(defaultValue = "false")
  private boolean preserveHeadersCase = false;

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

  public boolean isPreserveHeadersCase() {
    return preserveHeadersCase;
  }

  public static class Builder {

    private boolean followRedirects;
    private HttpSendBodyMode sendBodyMode;
    private HttpStreamingType requestStreamingMode;
    private boolean enableCookies;
    private List<RequestHeader> defaultHeaders;
    private List<QueryParam> defaultQueryParams;
    private OutboundCorrelationStrategy sendCorrelationId = AUTO;
    private boolean preserveHeadersCase = false;

    public Builder withFollowRedirects(boolean followRedirects) {
      this.followRedirects = followRedirects;
      return this;
    }

    public Builder withHttpSendBodyMode(HttpSendBodyMode sendBodyMode) {
      this.sendBodyMode = sendBodyMode;
      return this;
    }

    public Builder withHttpStreamingType(HttpStreamingType requestStreamingMode) {
      this.requestStreamingMode = requestStreamingMode;
      return this;
    }

    public Builder withEnableCookies(boolean enableCookies) {
      this.enableCookies = enableCookies;
      return this;
    }

    public Builder withDefaultHeaders(List<RequestHeader> defaultHeaders) {
      this.defaultHeaders = defaultHeaders;
      return this;
    }

    public Builder withDefaultQueryParams(List<QueryParam> defaultQueryParams) {
      this.defaultQueryParams = defaultQueryParams;
      return this;
    }

    public Builder withOutboundCorrelationStrategy(OutboundCorrelationStrategy sendCorrelationId) {
      this.sendCorrelationId = sendCorrelationId;
      return this;
    }

    public Builder withPreserveHeadersCase(boolean preserveHeadersCase) {
      this.preserveHeadersCase = preserveHeadersCase;
      return this;
    }

    public RequestSettings build() {
      RequestSettings settings = new RequestSettings();
      settings.followRedirects = this.followRedirects;
      settings.sendBodyMode = this.sendBodyMode;
      settings.requestStreamingMode = this.requestStreamingMode;
      settings.enableCookies = this.enableCookies;
      settings.defaultHeaders = this.defaultHeaders;
      settings.defaultQueryParams = this.defaultQueryParams;
      settings.sendCorrelationId = this.sendCorrelationId;
      settings.preserveHeadersCase = this.preserveHeadersCase;
      return settings;
    }



    public static Builder newInstance() {
      return new Builder();
    }
  }
}
