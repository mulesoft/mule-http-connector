/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import static org.mule.extension.http.api.error.HttpError.getErrorByCode;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpError;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.io.InputStream;
import java.util.Optional;

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

  protected boolean belongs(int value) {
    String[] valueParts = values.split(",");

    for (String valuePart : valueParts) {
      if (valuePart.contains("..")) {
        String[] limits = valuePart.split("\\.\\.");
        int lower = Integer.parseInt(limits[0]);
        int upper = Integer.parseInt(limits[1]);

        if (value >= lower && value <= upper) {
          return true;
        }
      } else {
        int code = Integer.parseInt(valuePart);

        if (code == value) {
          return true;
        }
      }
    }

    return false;
  }

  public String getValues() {
    return values;
  }

  public void setValues(String values) {
    this.values = values;
  }

  /**
   * Builds the exception message for a rejected status code based on the {@link HttpRequest} that triggered it. The message will
   * follow the following pattern: "HTTP GET on resource http://host:port/path failed", adding a custom message for known errors
   * and the received status code for all errors.
   *
   * @param status the status code received as a response
   * @param request the {@link HttpRequest} that resulted in the failure response
   * @return the message to be used on exceptions
   */
  protected String getExceptionMessage(int status, HttpRequest request) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("HTTP ").append(request.getMethod()).append(" on resource ").append(request.getUri()).append(" failed");
    Optional<HttpError> httpError = getErrorByCode(status);
    if (httpError.isPresent()) {
      stringBuilder.append(": ").append(httpError.get().getErrorMessage(request)).append(" (").append(status).append(")");
    } else {
      stringBuilder.append(" with status code ").append(status);
    }
    stringBuilder.append(".");

    return stringBuilder.toString();
  }

  /**
   * Creates the exception to be thrown if the validation didn't pass.
   * 
   * @param result the result of the request operation
   * @param request the HTTP request sent
   * @param status the HTTP response status code
   * @return
   * @throws ResponseValidatorTypedException
   */
  protected void throwValidationException(Result<InputStream, HttpResponseAttributes> result, HttpRequest request, int status) {
    Optional<HttpError> error = getErrorByCode(status);
    if (error.isPresent()) {
      throw new ResponseValidatorTypedException(getExceptionMessage(status, request), error.get(), result);
    } else {
      throw new ResponseValidatorException(getExceptionMessage(status, request), result);
    }
  }

}
