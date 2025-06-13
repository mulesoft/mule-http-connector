/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.Map;
import java.util.Objects;

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

  public BaseHttpRequestAttributes(MultiMap<String, String> headers, MultiMap<String, String> queryParams,
                                   Map<String, String> uriParams, String requestPath) {
    super(headers);
    this.queryParams = queryParams;
    this.uriParams = uriParams;
    this.requestPath = requestPath;
  }

  public String getRequestPath() {
    return requestPath;
  }

  public MultiMap<String, String> getQueryParams() {
    return queryParams;
  }

  public Map<String, String> getUriParams() {
    return uriParams;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(queryParams, requestPath, uriParams);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BaseHttpRequestAttributes other = (BaseHttpRequestAttributes) obj;
    return Objects.equals(queryParams, other.queryParams)
        && Objects.equals(requestPath, other.requestPath)
        && Objects.equals(uriParams, other.uriParams);
  }


}
