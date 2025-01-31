/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.client;

import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;

import org.mule.runtime.api.util.MultiMap;

/**
 * Attributes of the response received by an HTTP Client operation, which are defined in {@link HTTPMockClientOperations}.
 */
public class HttpClientResponseAttributes {

  private int statusCode;
  private String reasonPhrase;
  private MultiMap<String, String> headers = emptyMultiMap();

  public HttpClientResponseAttributes() {}

  public MultiMap<String, String> getHeaders() {
    return headers;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public void setHeaders(MultiMap<String, String> headers) {
    this.headers = headers;
  }

  public void setReasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
  }

  @Override
  public String toString() {
    return "HttpClientResponseAttributes{" +
        "statusCode=" + statusCode +
        ", reasonPhrase='" + reasonPhrase + '\'' +
        ", headers=" + headers +
        '}';
  }
}
