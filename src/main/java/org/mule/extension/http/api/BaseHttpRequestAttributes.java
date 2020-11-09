/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.Map;

/**
 * Common parts of all http request attributes.
 *
 * @since 1.0
 */
public class BaseHttpRequestAttributes extends HttpAttributes {

  private static final long serialVersionUID = -3580630130730447236L;

  /**
   * Query parameters map built from the parsed string. Former 'http.query.params'.
   */
  @Parameter
  protected MultiMap<String, String> queryParams;

  /**
   * URI parameters extracted from the request path. Former 'http.uri.params'.
   */
  @Parameter
  protected Map<String, String> uriParams;

  /**
   * Full path requested. Former 'http.request.path'.
   */
  @Parameter
  protected String requestPath;

  /**
   * Full url to use for the request.
   */
  @Parameter
  protected String url;

  public BaseHttpRequestAttributes(MultiMap<String, String> headers, MultiMap<String, String> queryParams,
                                   Map<String, String> uriParams, String requestPath) {
    this(headers, queryParams, uriParams, requestPath, null);
  }

  public BaseHttpRequestAttributes(MultiMap<String, String> headers, MultiMap<String, String> queryParams,
                                   Map<String, String> uriParams, String requestPath, String url) {
    super(headers);
    this.queryParams = queryParams;
    this.uriParams = uriParams;
    this.requestPath = requestPath;
    this.url = url;
  }

  public String getRequestPath() {
    return requestPath;
  }

  public String getUrl() {
    return url;
  }

  public MultiMap<String, String> getQueryParams() {
    return queryParams;
  }

  public Map<String, String> getUriParams() {
    return uriParams;
  }
}
