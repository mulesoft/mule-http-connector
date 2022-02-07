/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;


import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.io.InputStream;
import java.util.function.IntConsumer;

/**
 * Response validator that allows specifying which status codes will be considered as successful. Other status codes in the
 * response will cause the component to throw an exception.
 *
 * @since 1.0
 */
@TypeDsl(allowTopLevelDefinition = true)
public class SuccessStatusCodeValidator extends RangeStatusCodeValidator {

  public SuccessStatusCodeValidator() {}

  public SuccessStatusCodeValidator(String values) {
    setValues(values);
  }

  @Override
  public void validate(Result<InputStream, HttpResponseAttributes> result, HttpRequest request) {
    validate(result, status -> throwValidationException(result, request, status));
  }

  @Override
  public void validate(Result<InputStream, HttpResponseAttributes> result, HttpRequest request, StreamingHelper streamingHelper) {
    validate(result, status -> throwValidationException(toMessage(result, streamingHelper), request, status));
  }

  private void validate(Result<InputStream, HttpResponseAttributes> result, IntConsumer ifInvalid) {
    int status = getStatusCode(result);

    if (!belongs(status)) {
      ifInvalid.accept(status);
    }
  }
}
