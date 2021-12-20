/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.response;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.extension.api.runtime.parameter.Literal;

import java.util.Optional;

/**
 * Component to use as {@link org.mule.runtime.extension.api.annotation.param.ParameterGroup} for the expressions
 * needed in the {@link HttpPollingSourceExpressions} to apply to the response
 * @since 1.7
 */
public final class HttpPollingSourceExpressions {

  /**
   * The split expression to apply to the response
   */
  @Parameter
  @org.mule.runtime.extension.api.annotation.param.Optional
  @Text
  @DisplayName("Split Expression")
  private Literal<String> splitExpression;

  public Optional<String> getSplitExpression() {
    return splitExpression.getLiteralValue();
  }

}
