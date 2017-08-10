/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors.parameters;

import org.mule.modules.cors.query.KernelTestParameters;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.HttpConstants.Method;
import org.mule.test.http.functional.listener.intercepting.cors.runner.CorsHttpEndpoint;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

public class CorsHttpParameters extends KernelTestParameters {

  public CorsHttpParameters(Method method, MultiMap<String, String> headers) {
    super(asString(method), headers);
  }

  public HttpUriRequest buildRequest(String port, CorsHttpEndpoint endpoint) {
    RequestBuilder request = request(uri(port, endpoint.path()));
    headers.keySet().forEach(header -> request.addHeader(header, headers.get(header)));
    return request.build();
  }

  private String uri(String port, String path) {
    return "http://localhost:" + port + "/" + path;
  }

  protected RequestBuilder request(String uri) {
    return RequestBuilder.create(method.toString()).setUri(uri);
  }

}
