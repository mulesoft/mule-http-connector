/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.el.ExpressionLanguage;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.Iterator;

import static java.lang.String.format;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.metadata.DataType.BOOLEAN;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_XML;
import static org.mule.runtime.core.api.util.StringUtils.isBlank;

/**
 * Utils class for splitting payloads.
 *
 * @since 1.0
 */
public final class SplitUtils {

  private SplitUtils() {

  }

  /**
   * Splits a payload handling its media type and the needed expressions automatically.
   *
   * @param expressionLanguage The instance of the expression language to use.
   * @param payload The payload to split.
   * @return An iterator containing an element for each item in the split payload.
   */
  public static Iterator<TypedValue<?>> split(ExpressionLanguage expressionLanguage, TypedValue<?> payload) {
    return split(expressionLanguage, payload, null);
  }

  /**
   * Splits a payload handling its media type and the needed expressions automatically.
   *
   * @param expressionLanguage The instance of the expression language to use.
   * @param payload The payload to split.
   * @param previousExpression An expression provided by the user that will be pointed to if the split fails to provide better
   *        error context.
   * @return An iterator containing an element for each item in the split payload.
   */
  public static Iterator<TypedValue<?>> split(ExpressionLanguage expressionLanguage, TypedValue<?> payload,
                                              String previousExpression) {
    if (payload.getDataType().getMediaType().matches(APPLICATION_XML)) {
      validatePayload(expressionLanguage, payload, "#[payload is Object]", previousExpression);
      return expressionLanguage.split("#[payload[0] default []]", buildContext(payload));
    } else {
      validatePayload(expressionLanguage, payload, "#[payload is Array]", previousExpression);
      return expressionLanguage.split("#[payload]", buildContext(payload));
    }
  }

  private static void validatePayload(ExpressionLanguage expressionLanguage, TypedValue<?> payload, String expression,
                                      String previousExpression) {
    TypedValue<?> result = expressionLanguage.evaluate(expression, BOOLEAN, buildContext(payload));
    if (result.getValue().equals(false)) {
      final String stringMessage = isBlank(expression)
          ? format("Failed to split payload that is not an array. Expression: '%s'. Result Payload: '%s'.", previousExpression,
                   payload.getValue())
          : format("Failed to split payload that is not an array. Result Payload: '%s'.", payload.getValue());
      throw new MuleRuntimeException(createStaticMessage(stringMessage));
    }
  }

  private static BindingContext buildContext(TypedValue<?> payload) {
    return BindingContext.builder().addBinding("payload", payload).build();
  }

}
