/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import org.mule.runtime.core.api.message.BaseAttributes;
import org.mule.runtime.api.util.MultiMap;

/**
 * Base representation of HTTP message attributes.
 *
 * @since 1.0
 */
public abstract class HttpAttributes extends BaseAttributes {

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
}
