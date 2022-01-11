/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.internal.request.HttpPollingSource.WATERMARK_PLACEHOLDER;
import static org.mule.runtime.core.api.el.ExpressionManager.DEFAULT_EXPRESSION_POSTFIX;
import static org.mule.runtime.core.api.el.ExpressionManager.DEFAULT_EXPRESSION_PREFIX;

import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.el.ExpressionLanguage;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.runtime.parameter.Literal;

import java.io.Serializable;

/**
 * Utils to be used regarding resolution of {@link Literal}s
 *
 * @since 1.7
 */
public final class LiteralExpressionUtils {

  private LiteralExpressionUtils() {
    // Empty private constructor to avoid instantiation.
  }

  /**
   * Resolves the real ({@link String}) value of a {@link Literal}.
   *
   * @param toResolve the {@link Literal} to be resolved
   * @param expressionLanguage
   * @param watermark the value of the watermark, that could be used in an expression within the {@link Literal}
   * @return If the {@link Literal}'s value is an expression, returns the value of resolving the expression.
   * If it is not an expression, then it returns the value as is.
   */
  public static String resolveLiteralExpression(Literal<String> toResolve, ExpressionLanguage expressionLanguage,
                                                Serializable watermark) {
    String raw = toResolve.getLiteralValue().orElse("");
    if (!isExpression(raw)) {
      return raw;
    }
    BindingContext context = BindingContext.builder().addBinding(WATERMARK_PLACEHOLDER, TypedValue.of(watermark)).build();
    TypedValue<?> result = expressionLanguage.evaluate(raw, context);
    return result.getValue() != null ? result.getValue().toString() : null;
  }

  private static boolean isExpression(String value) {
    String trim = value.trim();
    return trim.startsWith(DEFAULT_EXPRESSION_PREFIX) && trim.endsWith(DEFAULT_EXPRESSION_POSTFIX);
  }
}
