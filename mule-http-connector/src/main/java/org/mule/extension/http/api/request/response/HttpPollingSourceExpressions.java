/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.response;

import static java.util.Optional.empty;
import static org.mule.runtime.api.meta.ExpressionSupport.REQUIRED;

import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.runtime.parameter.Literal;

import java.util.Optional;

/**
 * Component to use as {@link org.mule.runtime.extension.api.annotation.param.ParameterGroup} for the expressions needed in the
 * {@link HttpPollingSourceExpressions} to apply to the response
 *
 * @since 1.8
 */
public final class HttpPollingSourceExpressions {

  /**
   * The split expression to apply to the response
   */
  @Parameter
  @org.mule.runtime.extension.api.annotation.param.Optional
  @Expression(REQUIRED)
  @DisplayName("Split Expression")
  private Literal<String> splitExpression;

  /**
   * The expression to retrieve the watermark from the (splitted) response
   */
  @Parameter
  @org.mule.runtime.extension.api.annotation.param.Optional
  @Expression(REQUIRED)
  @DisplayName("Watermark Expression")
  private Literal<String> watermarkExpression;

  /**
   * The expression to retrieve the id from every (splitted) item
   */
  @Parameter
  @org.mule.runtime.extension.api.annotation.param.Optional
  @Expression(REQUIRED)
  @DisplayName("Id Expression")
  private Literal<String> idExpression;

  public Optional<String> getSplitExpression() {
    return !isBlankExpression(splitExpression) ? splitExpression.getLiteralValue() : empty();
  }

  public Optional<String> getWatermarkExpression() {
    return !isBlankExpression(watermarkExpression) ? watermarkExpression.getLiteralValue() : empty();
  }

  public Optional<String> getIdExpression() {
    return !isBlankExpression(idExpression) ? idExpression.getLiteralValue() : empty();
  }

  private boolean isBlankExpression(Literal<String> expression) {
    if (expression == null) {
      return true;
    }
    Optional<String> exp = expression.getLiteralValue();
    return !exp.isPresent() || exp.get().trim().equals("");
  }

}
