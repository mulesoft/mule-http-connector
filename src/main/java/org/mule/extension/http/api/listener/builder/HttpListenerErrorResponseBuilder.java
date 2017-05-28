/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.builder;

import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSES;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link HttpListenerResponseBuilder} which returns error responses
 *
 * @since 1.0
 */
public class HttpListenerErrorResponseBuilder extends HttpListenerResponseBuilder {

  /**
   * The body of the response message.
   */
  @Parameter
  @Optional(defaultValue = "#[error.description]")
  @Content
  @Placement(tab = RESPONSES, order = 1)
  private TypedValue<Object> body;

  /**
   * HTTP headers the message should include.
   */
  @Parameter
  @Optional
  @Content
  @Placement(tab = RESPONSES, order = 2)
  protected Map<String, String> headers = new HashMap<>();

  /**
   * HTTP status code the response should have.
   */
  @Parameter
  @Optional
  @Placement(tab = RESPONSES, order = 3)
  private Integer statusCode;

  /**
   * HTTP reason phrase the response should have.
   */
  @Parameter
  @Optional
  @Placement(tab = RESPONSES, order = 4)
  private String reasonPhrase;

  public Integer getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public void setReasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
  }

  @Override
  public TypedValue<Object> getBody() {
    return body;
  }

  @Override
  public void setBody(TypedValue<Object> body) {
    this.body = body;
  }

  @Override
  public Map<String, String> getHeaders() {
    return headers;
  }

  @Override
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

}
