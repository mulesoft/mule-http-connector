/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.builder;

import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSES;
import static org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap.emptyCaseInsensitiveMultiMap;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import shapeless.PolyDefns;

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
  @Optional(defaultValue = "#[output text/plain --- error.description]")
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
  @NullSafe
  protected CaseInsensitiveMultiMap headers = emptyCaseInsensitiveMultiMap();

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

  @Override
  public Integer getStatusCode() {
    return statusCode;
  }

  @Override
  public String getReasonPhrase() {
    return reasonPhrase;
  }

  @Override
  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  @Override
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
  public CaseInsensitiveMultiMap getHeaders() {
    return headers;
  }

  @Override
  public void setHeaders(CaseInsensitiveMultiMap headers) {
    if (headers != null) {
      this.headers = headers;
    } else {
      this.headers = emptyCaseInsensitiveMultiMap();
    }
  }

}
