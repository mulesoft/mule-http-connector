/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import static java.util.Optional.empty;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.functional.api.exception.ExpectedError.none;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.RESPONSE_VALIDATION;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import java.io.InputStream;

@Feature(HTTP_EXTENSION)
@Story(RESPONSE_VALIDATION)
@Issue("HTTPC-187")
public class ExpressionResponseValidatorTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public ExpectedError expectedError = none();

  @Override
  protected String getConfigFile() {
    return "http-request-response-validator-config.xml";
  }

  @Test
  public void payloadStreamIsManaged() throws Exception {
    assertThat(flowRunner("repeatableStreamingFlow").keepStreamsOpen().run().getMessage(),
               hasPayload(equalTo(DEFAULT_RESPONSE)));
  }

  @Test
  public void globalResponseValidator() throws Exception {
    assertThat(flowRunner("globalValidatorFlow").keepStreamsOpen().run().getMessage(),
               hasPayload(equalTo(DEFAULT_RESPONSE)));
  }

  @Test
  public void responseValidatorReturningNonBooleanValue() throws Exception {
    expectedError.expectErrorType("HTTP", "BAD_REQUEST");
    expectedError.expectCause(instanceOf(ResponseValidatorTypedException.class));
    expectedError.expectCause(hasMessage(containsString("The expression '#[4]' returned a non boolean value")));
    flowRunner("nonBooleanValidatorFlow").run();
  }

  @Test
  public void expressionEvaluatingToFalse() throws Exception {
    expectedError.expectErrorType("HTTP", "BAD_REQUEST");
    expectedError.expectCause(instanceOf(ResponseValidatorTypedException.class));
    expectedError.expectCause(hasMessage(containsString("The expression '#[false]' evaluated to false")));
    flowRunner("falseExpressionFlow").run();
  }

  @Test(expected = IllegalStateException.class)
  public void whenLiteralExpressionIsNotPresentTheValidatorThrowsException() {
    ExpressionResponseValidator validator = new ExpressionResponseValidator();
    Literal<String> mockLiteral = mock(Literal.class);
    when(mockLiteral.getLiteralValue()).thenReturn(empty());
    validator.setExpression(mockLiteral);

    Result<InputStream, HttpResponseAttributes> result = mock(Result.class);
    HttpRequest request = mock(HttpRequest.class);
    validator.validate(result, request);
  }
}
