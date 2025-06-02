/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import static java.lang.String.format;
import static org.mule.extension.http.api.error.HttpError.BAD_REQUEST;
import static org.mule.runtime.api.el.BindingContext.builder;
import static org.mule.runtime.api.metadata.DataType.BOOLEAN;
import static org.mule.runtime.api.metadata.DataType.fromType;
import static org.mule.runtime.api.metadata.MediaType.ANY;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * Response validator that uses an expression.
 *
 * @since 1.8
 */
@TypeDsl(allowTopLevelDefinition = true)
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
      // This should never happen because the parameter isn't optional, but the Literal interface returns an Optional
      // so this check is needed.
      throw new IllegalStateException("The expression literal value hasn't been provided");
    }

    BindingContext bindingContext = buildBindingContext(result);
    TypedValue<?> validationResult = expressionManager.evaluate(optExpression.get(), bindingContext);
    if (!BOOLEAN.isCompatibleWith(validationResult.getDataType())) {
      throw new ResponseValidatorTypedException(format("The expression '%s' returned a non boolean value", optExpression.get()),
                                                BAD_REQUEST);
    }

    if (!((Boolean) validationResult.getValue()).booleanValue()) {
      throw new ResponseValidatorTypedException(format("The expression '%s' evaluated to false", optExpression.get()),
                                                BAD_REQUEST);
    }
  }

  private static BindingContext buildBindingContext(Result<? extends InputStream, ? extends HttpResponseAttributes> result) {
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

  @Override
  public boolean mayConsumeBody() {
    return true;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ExpressionResponseValidator)) {
      return false;
    }
    ExpressionResponseValidator otherValidator = (ExpressionResponseValidator) other;
    return Objects.equals(expression, otherValidator.expression);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expression);
  }
}
