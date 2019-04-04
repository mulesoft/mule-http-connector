/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import static java.lang.Integer.parseInt;
import static org.mule.extension.http.api.error.HttpError.getErrorByCode;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.core.api.util.ClassUtils.memoize;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpErrorMessageGenerator;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Base status code validator that can be extended to create custom validations.
 *
 * @since 1.0
 */
public abstract class RangeStatusCodeValidator implements ResponseValidator {

  /**
   * Status codes that will be considered.
   */
  @Parameter
  private String values;

  private Function<Integer, Boolean> belongsFunction = memoize(value -> {
    String[] valueParts = values.split(",");

    for (String valuePart : valueParts) {
      if (valuePart.contains("..")) {
        String[] limits = valuePart.split("\\.\\.");
        int lower = parseInt(limits[0].trim());
        int upper = parseInt(limits[1].trim());

        if (value >= lower && value <= upper) {
          return true;
        }
      } else {
        int code = parseInt(valuePart.trim());

        if (code == value) {
          return true;
        }
      }
    }

    return false;
  }, new ConcurrentHashMap<>());

  private HttpErrorMessageGenerator errorMessageGenerator = new HttpErrorMessageGenerator();

  protected boolean belongs(int value) {
    return belongsFunction.apply(value);
  }

  public String getValues() {
    return values;
  }

  public void setValues(String values) {
    this.values = values;
  }

  /**
   * Creates the exception to be thrown if the validation didn't pass.
   *
   * @param result the result of the request operation
   * @param request the HTTP request sent
   * @param status the HTTP response status code
   * @throws ResponseValidatorTypedException
   * @throws ResponseValidatorException
   */
  protected void throwValidationException(Result<InputStream, HttpResponseAttributes> result, HttpRequest request, int status) {
    getErrorByCode(status)
        .map(error -> {
          throw new ResponseValidatorTypedException(errorMessageGenerator.createFrom(request, status), error, result);
        })
        .orElseThrow(
                     () -> new ResponseValidatorException(errorMessageGenerator.createFrom(request, status), result));
  }

  /**
   * Creates the exception to be thrown if the validation didn't pass.
   *
   * @param message the message for the exception
   * @param request the HTTP request sent
   * @param status the HTTP response status code
   * @throws ResponseValidatorTypedException
   * @throws ResponseValidatorException
   */
  protected void throwValidationException(Message message, HttpRequest request, int status) {
    getErrorByCode(status)
        .map(error -> {
          throw new ResponseValidatorTypedException(errorMessageGenerator.createFrom(request, status), error, message);
        })
        .orElseThrow(
                     () -> new ResponseValidatorException(errorMessageGenerator.createFrom(request, status), message));
  }

  protected Message toMessage(Result<InputStream, HttpResponseAttributes> result, StreamingHelper streamingHelper) {
    return Message.builder()
        .value(streamingHelper.resolveCursorProvider(result.getOutput()))
        .attributesValue(result.getAttributes().get())
        .mediaType(result.getMediaType().orElse(ANY))
        .build();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RangeStatusCodeValidator) {
      return Objects.equals(values, ((RangeStatusCodeValidator) obj).values);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(values);
  }
}
