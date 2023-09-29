/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.Objects;

import static java.util.Objects.hash;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

/**
 * Represents an HTTP Query Parameter
 *
 * @since 1.8
 */
@Alias("queryParam")
public class TestQueryParam implements KeyValuePair {

  /**
   * Represents the Key of this HTTP Query Parameter
   */
  @Parameter
  @Expression(value = NOT_SUPPORTED)
  private String key;

  /**
   * Represents the Value of this HTTP Query Parameter
   */
  @Parameter
  @Expression(value = NOT_SUPPORTED)
  private String value;

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TestQueryParam) {
      TestQueryParam other = (TestQueryParam) obj;
      return Objects.equals(key, other.key) && Objects.equals(value, other.value);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return hash(key, value);
  }
}
