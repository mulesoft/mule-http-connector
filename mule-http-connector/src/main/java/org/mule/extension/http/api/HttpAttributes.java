/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.lang.System.lineSeparator;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Base representation of HTTP message attributes.
 *
 * @since 1.0
 */
public abstract class HttpAttributes implements Serializable {

  private static final long serialVersionUID = -5172640902396741873L;

  public static final String TAB = "   ";
  public static final String DOUBLE_TAB = TAB + TAB;

  /**
   * Map of HTTP headers in the message. Former properties.
   */
  @Parameter
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

  private static String formatHttpAttributesMapsToString(String name, Stream<Map.Entry<String, String>> stream) {
    StringBuilder builder = new StringBuilder();
    builder.append(TAB).append(name).append("=[").append(lineSeparator());
    stream.forEach(element -> builder.append(DOUBLE_TAB)
        .append(element.getKey()).append("=").append(obfuscateValueIfNecessary(element)).append(lineSeparator()));
    builder.append(TAB).append("]").append(lineSeparator());
    return builder.toString();
  }

  private static String obfuscateValueIfNecessary(Map.Entry<String, String> entry) {
    String key = entry.getKey();
    if (key.equals("password") || key.equals("pass") || key.contains("secret") || key.contains("authorization")) {
      return "****";
    } else {
      return entry.getValue();
    }
  }

  public static StringBuilder buildMapToString(Map map, String name, Stream stream, StringBuilder builder) {
    if (map.isEmpty()) {
      builder.append(TAB).append(name).append("=[]").append(lineSeparator());
      return builder;
    }
    builder.append(formatHttpAttributesMapsToString(name, stream));
    return builder;
  }

  @Override
  public int hashCode() {
    return Objects.hash(headers);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    HttpAttributes other = (HttpAttributes) obj;
    return Objects.equals(headers, other.headers);
  }
}
