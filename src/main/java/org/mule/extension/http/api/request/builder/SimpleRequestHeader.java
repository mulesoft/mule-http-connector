/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import static java.util.Objects.hash;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.parameter.Literal;

import java.util.Objects;

/**
 * Represents an HTTP Header
 *
 * @since 1.7
 */
@Alias("requestHeader")
public class SimpleRequestHeader implements KeyValuePair {

  /**
   * Represents the Key of this HTTP Header
   */
  @Parameter
  private String key;

  /**
   * Represents the Value of this HTTP Header, that could be an expression depending on a watermarking value
   * that would be then resolved
   */
  @Parameter
  private Literal<String> value;

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getValue() {
    return value.getLiteralValue().orElse("");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimpleRequestHeader that = (SimpleRequestHeader) o;
    return Objects.equals(key, that.key) &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return hash(key, value);
  }

}
