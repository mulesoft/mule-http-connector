/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.regex.Matcher.quoteReplacement;
import static org.mule.extension.http.internal.HttpConnectorConstants.ENCODE_URI_PARAMS_PROPERTY;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.extension.api.runtime.parameter.OutboundCorrelationStrategy.AUTO;
import static org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap.emptyCaseInsensitiveMultiMap;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;
import org.mule.extension.http.api.HttpMessageBuilder;
import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.runtime.api.exception.MuleRuntimeException;
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
import org.mule.runtime.http.api.domain.AbstractCaseInsensitiveMultiMap;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Component that specifies how to create a proper HTTP request.
 *
 * @since 1.0
 */
public class HttpRequesterRequestBuilder extends HttpMessageBuilder {

  private static boolean ENCODE_URI_PARAMS = getBoolean(ENCODE_URI_PARAMS_PROPERTY);

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
  protected CaseInsensitiveMultiMap headers = emptyCaseInsensitiveMultiMap();

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

  private HttpRequestBuilder reqBuilder;

  @Override
  public TypedValue<Object> getBody() {
    return body;
  }

  @Override
  public void setBody(TypedValue<Object> body) {
    this.body = body;
  }

  @Override
  public AbstractCaseInsensitiveMultiMap getHeaders() {
    return headers;
  }

  @Override
  public void setHeaders(AbstractCaseInsensitiveMultiMap headers) {
    this.headers = (CaseInsensitiveMultiMap) headers;
  }

  public String replaceUriParams(String path) {
    for (String uriParamName : uriParams.keySet()) {
      String uriParamValue = uriParams.get(uriParamName);

      if (uriParamValue == null) {
        throw new NullPointerException(format("Expression {%s} evaluated to null.", uriParamName));
      }
      if (ENCODE_URI_PARAMS) {
        try {
          uriParamValue = encode(uriParamValue, UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
          throw new MuleRuntimeException(createStaticMessage("Could not encode URI parameter '%s'", uriParamValue), e);
        }
      }
      path = path.replaceAll("\\{" + uriParamName + "\\}", quoteReplacement(uriParamValue));
    }
    return path;
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

  public CorrelationInfo getCorrelationInfo() {
    return correlationInfo;
  }

  public void setCorrelationInfo(CorrelationInfo correlationInfo) {
    this.correlationInfo = correlationInfo;
  }

  public OutboundCorrelationStrategy getSendCorrelationId() {
    return sendCorrelationId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public HttpRequestBuilder configure(HttpRequesterConfig config) {
    return HttpRequest.builder(PRESERVE_HEADER_CASE || config.isPreserveHeadersCase())
        .headers(headers)
        .queryParams(queryParams);
  }

  public static void refreshSystemProperties() {
    ENCODE_URI_PARAMS = getBoolean(ENCODE_URI_PARAMS_PROPERTY);
  }


}
