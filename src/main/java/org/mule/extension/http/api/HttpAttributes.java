/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.lang.System.lineSeparator;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import org.mule.runtime.api.util.MultiMap;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Base representation of HTTP message attributes.
 *
 * @since 1.0
 */
public abstract class HttpAttributes implements Serializable {

  protected static final String tab = "   ";
  protected static final String doubleTab = tab + tab;

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

  protected String formatHttpAttributesMapsToString(String name, Stream<Map.Entry<String, String>> stream) {
    StringBuilder builder = new StringBuilder();
    builder.append(tab).append(name).append("=[").append(lineSeparator());
    stream.forEach(element -> builder.append(doubleTab)
        .append(element.getKey()).append("=").append(element.getValue()).append(lineSeparator()));
    builder.append(tab).append("]").append(lineSeparator());
    return builder.toString();
  }

  protected StringBuilder buildMapToString(Map map, String name, Stream stream, StringBuilder builder) {
    if (map == null || map.isEmpty()) {
      builder.append(tab).append(name).append("=[]").append(lineSeparator());
      return builder;
    }
    builder.append(formatHttpAttributesMapsToString(name, stream));
    return builder;
  }
}
