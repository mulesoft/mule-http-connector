/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.server;

import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.io.InputStream;

/**
 * Response representation in the server side. The server-endpoint source can configure the parameters defined here.
 */
public class HTTPMockServerResponse {

  @Parameter
  @Optional(defaultValue = "200")
  private Integer statusCode;

  @Parameter
  @Optional(defaultValue = "OK")
  private String reasonPhrase;

  @Parameter
  @Content(primary = true)
  private TypedValue<InputStream> body;

  @Parameter
  @Optional
  @Content
  private MultiMap<String, String> headers = emptyMultiMap();

  public Integer getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public MultiMap<String, String> getHeaders() {
    return headers;
  }

  public TypedValue<InputStream> getBody() {
    return body;
  }

  public void setBody(TypedValue<InputStream> body) {
    this.body = body;
  }

  public void setHeaders(MultiMap<String, String> headers) {
    this.headers = headers;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public void setReasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
  }
}
