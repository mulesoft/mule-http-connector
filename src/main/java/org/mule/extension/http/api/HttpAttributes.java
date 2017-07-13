/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import org.mule.runtime.api.util.MultiMap;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Base representation of HTTP message attributes.
 *
 * @since 1.0
 */
public abstract class HttpAttributes implements Serializable {

  /**
   * Map of HTTP headers in the message. Former properties.
   */
  protected MultiMap<String, String> headers;

  public HttpAttributes(MultiMap<String, String> headers) {
    this.headers = headers.toImmutableMultiMap();
  }

  public MultiMap<String, String> getHeaders() {
    return headers;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, SHORT_PREFIX_STYLE);
  }
}
