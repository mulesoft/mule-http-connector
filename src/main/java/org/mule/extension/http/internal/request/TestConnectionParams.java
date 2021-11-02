/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.util.Collections.emptyMap;
import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSE;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.DEFAULT_TAB;

import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import java.util.Map;

public class TestConnectionParams {

  /**
   * URL where to send the request.
   */
  @Parameter
  @Optional
  @DisplayName("URL")
  @Example("http://www.mulesoft.com")
  @Placement(order = 1)
  private String url;

  /**
   * Path where the request will be sent.
   */
  @Parameter
  @Optional
  @Placement(order = 2)
  private String path = "/";

  /**
   * HTTP Method for the request to be sent.
   */
  @Parameter
  @Optional(defaultValue = "GET")
  @Placement(order = 3)
  private String method;

  /**
   * The body of the request message
   */
  @Parameter
  @Optional
  @Placement(order = 4)
  private TypedValue<Object> body;

  /**
   * HTTP headers the message should include.
   */
  @Parameter
  @Optional
  @Placement(order = 5)
  @NullSafe
  protected MultiMap<String, String> headers = emptyMultiMap();

  /**
   * URI parameters that should be used to create the request.
   */
  @Parameter
  @Optional
  @Placement(order = 6)
  @DisplayName("URI Parameters")
  private Map<String, String> uriParams = emptyMap();

  /**
   * Query parameters the request should include.
   */
  @Parameter
  @Optional
  @Placement(order = 7)
  @DisplayName("Query Parameters")
  private MultiMap<String, String> queryParams = emptyMultiMap();

  /**
   * Validates when a connection is successful based on the response.
   */
  @Parameter
  @Optional
  @Placement(order = 8)
  private ResponseValidator responseValidator;

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public TypedValue<Object> getBody() {
    return body;
  }

  public void setBody(TypedValue<Object> body) {
    this.body = body;
  }

  public MultiMap<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(MultiMap<String, String> headers) {
    this.headers = headers;
  }

  public Map<String, String> getUriParams() {
    return uriParams;
  }

  public void setUriParams(Map<String, String> uriParams) {
    this.uriParams = uriParams;
  }

  public MultiMap<String, String> getQueryParams() {
    return queryParams;
  }

  public void setQueryParams(MultiMap<String, String> queryParams) {
    this.queryParams = queryParams;
  }

  public ResponseValidator getResponseValidator() {
    return responseValidator;
  }

  public void setResponseValidator(ResponseValidator responseValidator) {
    this.responseValidator = responseValidator;
  }
}
