/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.internal.request;

import static java.util.Optional.of;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.validator.FailureStatusCodeValidator;
import org.mule.extension.http.api.request.validator.RangeStatusCodeValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Validates the behavior of the different status code validators in use and the inner parsing of the values, hence
 * the spaces in some of them.
 */
public class StatusCodeValidatorTestCase extends AbstractMuleTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private Result mockResult = mock(Result.class);
  private HttpResponseAttributes mockAttributes = mock(HttpResponseAttributes.class);
  private HttpRequest mockRequest = mock(HttpRequest.class);
  private SuccessStatusCodeValidator successValidator = new SuccessStatusCodeValidator();
  private FailureStatusCodeValidator failureValidator = new FailureStatusCodeValidator();

  @Before
  public void setUp() {
    when(mockResult.getAttributes()).thenReturn(of(mockAttributes));
  }

  @Test
  public void successAcceptsInRange() throws Exception {
    validateStatusFor(successValidator, 401, "200..404");
  }

  @Test
  public void successRejectsOutRange() throws Exception {
    expectedException.expectMessage(contains("failed"));
    validateStatusFor(successValidator, 500, "200.. 404");
  }

  @Test
  public void successAcceptsSpecificMatch() throws Exception {
    validateStatusFor(successValidator, 204, "200, 204");
  }

  @Test
  public void successRejectsMismatch() throws Exception {
    expectedException.expectMessage(contains("failed"));
    validateStatusFor(successValidator, 201, "200,204");
  }

  @Test
  public void successAcceptsMixedRange() throws Exception {
    validateStatusFor(successValidator, 403, "200,204,401 ..404");
  }

  @Test
  public void successAcceptsMixedMatch() throws Exception {
    validateStatusFor(successValidator, 204, "200, 204,401..404");
  }

  @Test
  public void successRejectsMixed() throws Exception {
    expectedException.expectMessage(contains("failed"));
    validateStatusFor(successValidator, 406, " 200,204,401..404");
  }

  @Test
  public void failureAcceptsOutRange() throws Exception {
    validateStatusFor(failureValidator, 204, "400..599");
  }

  @Test
  public void failureRejectsInRange() throws Exception {
    expectedException.expectMessage(contains("failed"));
    validateStatusFor(failureValidator, 401, "400 ..599");
  }

  @Test
  public void failureAcceptsMismatch() throws Exception {
    validateStatusFor(failureValidator, 403, "401,404");
  }

  @Test
  public void failureRejectsSpecificMatch() throws Exception {
    expectedException.expectMessage(contains("failed"));
    validateStatusFor(failureValidator, 401, "401 ,404");
  }

  @Test
  public void failureRejectsMixedRange() throws Exception {
    expectedException.expectMessage(contains("failed"));
    validateStatusFor(failureValidator, 403, "201,204,401 ..404");
  }

  @Test
  public void failureRejectsMixedMatch() throws Exception {
    expectedException.expectMessage(contains("failed"));
    validateStatusFor(failureValidator, 204, "200, 204,401..404");
  }

  @Test
  public void failureAcceptsMixed() throws Exception {
    validateStatusFor(failureValidator, 406, " 200,204,401..404");
  }

  private void validateStatusFor(RangeStatusCodeValidator validator, int status, String values) throws Exception {
    when(mockAttributes.getStatusCode()).thenReturn(status);
    validator.setValues(values);
    validator.validate(mockResult, mockRequest);
  }

}
