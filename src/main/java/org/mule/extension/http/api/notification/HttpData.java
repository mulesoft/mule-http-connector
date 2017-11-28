/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.notification;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.message.HttpMessage;

/**
 * Base class for HTTP notification related data.
 *
 * @since 1.1
 */
public abstract class HttpData {

  private final MultiMap<String, String> headers;

  <T extends HttpMessage> HttpData(T message) {
    this.headers = createHeadersMultiMap(message);
  }

  public MultiMap<String, String> getHeaders() {
    return headers;
  }

  private MultiMap<String, String> createHeadersMultiMap(HttpMessage message) {
    MultiMap<String, String> headers = new MultiMap<>();
    message.getHeaderNames().forEach(key -> message.getHeaderValues(key).forEach(value -> headers.put(key, value)));
    return headers.toImmutableMultiMap();
  }

}
