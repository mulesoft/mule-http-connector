/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors.parameters;

import static org.mule.runtime.http.api.HttpConstants.Method.GET;

import org.mule.modules.cors.query.CorsTestQuery;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.test.http.functional.listener.intercepting.cors.runner.CorsHttpEndpoint;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

public class CorsParameters implements CorsTestQuery {

  private final HttpConstants.Method method;
  private final MultiMap<String, String> headers;

  public CorsParameters(HttpConstants.Method method, MultiMap<String, String> headers) {
    this.method = method != null ? method : GET;
    this.headers = headers;
  }

  public HttpUriRequest buildRequest(String port, CorsHttpEndpoint endpoint) {
    RequestBuilder request = request(uri(port, endpoint.path()));
    headers.keySet().forEach(header -> request.addHeader(header, headers.get(header)));
    return request.build();
  }

  private String uri(String port, String path) {
    return "http://localhost:" + port + "/" + path; // TODO should we parameterize the host?
  }

  protected RequestBuilder request(String uri) {
    return RequestBuilder.create(method.toString()).setUri(uri);
  }
}
