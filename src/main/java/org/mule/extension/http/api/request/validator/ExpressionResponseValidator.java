/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import static java.lang.String.format;
import static org.mule.extension.http.api.error.HttpError.BAD_REQUEST;
import static org.mule.runtime.api.el.BindingContext.builder;
import static org.mule.runtime.api.metadata.DataType.fromType;
import static org.mule.runtime.api.metadata.MediaType.ANY;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Optional;

/**
 * Response validator that uses an expression.
 *
 * @since 1.0
 */
public class ExpressionResponseValidator implements ResponseValidator {

  /**
   * DataWeave expression.
   */
  @Parameter
  private Literal<String> expression;

  @Inject
  private ExpressionManager expressionManager;

  @Override
  public void validate(Result<InputStream, HttpResponseAttributes> result, HttpRequest request) {
    Optional<String> optExpression = expression.getLiteralValue();
    if (!optExpression.isPresent()) {
      throw new ResponseValidatorTypedException("An expression is required", BAD_REQUEST);
    }

    BindingContext bindingContext = buildBindingContext(result);
    Boolean validationResult = (Boolean) expressionManager.evaluate(optExpression.get(), bindingContext).getValue();
    if (!validationResult.booleanValue()) {
      throw new ResponseValidatorTypedException(format("The expression '%s' evaluated to false", expression), BAD_REQUEST);
    }
  }

  private BindingContext buildBindingContext(Result<InputStream, HttpResponseAttributes> result) {
    InputStream payload = result.getOutput();
    HttpResponseAttributes attributes = result.getAttributes().orElse(null);
    MediaType mediaType = result.getMediaType().orElse(ANY);
    return builder()
        .addBinding("payload", new TypedValue<>(payload, fromType(InputStream.class)))
        .addBinding("attributes", new TypedValue<>(attributes, fromType(HttpResponseAttributes.class)))
        .addBinding("mediaType", new TypedValue<>(mediaType, fromType(MediaType.class)))
        .build();
  }

  public Literal<String> getExpression() {
    return expression;
  }

  public void setExpression(Literal<String> expression) {
    this.expression = expression;
  }

  @Override
  public boolean mayConsumeBody() {
    return true;
  }
}
