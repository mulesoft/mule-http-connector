/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import org.mule.runtime.api.el.ExpressionLanguage;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.parameter.Literal;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

import static org.mule.extension.http.internal.request.LiteralExpressionUtils.resolveLiteralExpression;

/**
 * Represents an HTTP Header
 *
 * @since 1.7
 */
@Alias("requestHeader")
public class SimpleRequestHeader implements KeyValuePair {

  @Parameter
  private String key;

  @Parameter
  private Literal<String> value;

  private ExpressionLanguage expressionLanguage;
  private Serializable watermark;

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getValue() {
    return resolveLiteralExpression(value, expressionLanguage, watermark);
  }

  public void setExpressionLanguage(ExpressionLanguage expressionLanguage) {
    this.expressionLanguage = expressionLanguage;
  }

  public void updateWatermark(Serializable watermark) {
    this.watermark = watermark;
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
    return Objects.hash(key, value);
  }

}
