/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.error;

import static org.mule.extension.http.api.error.HttpError.getErrorByCode;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.util.Optional;

/**
 * Component capable of generating rich error messages based on a failed {@link HttpRequest}, a message or the response status.
 *
 * @since 1.0
 */
public class HttpErrorMessageGenerator {

  /**
   * Builds the exception message for a failed {@link HttpRequest} based on it and an exception message. The message will follow
   * the following pattern: "HTTP GET on resource http://host:port/path failed", adding the specified message.
   *
   * @param request the {@link HttpRequest} that resulted in the failure
   * @param message the failure message
   * @return the message to be used on exceptions
   */
  public String createFrom(HttpRequest request, String message) {
    return getBaseMessage(request).append(": ").append(message).append(".").toString();
  }

  /**
   * Builds the exception message for a rejected status code based on the {@link HttpRequest} that triggered it. The message will
   * follow the following pattern: "HTTP GET on resource http://host:port/path failed", adding a custom message for known errors
   * and the received status code for all errors.
   *
   * @param request the {@link HttpRequest} that resulted in the failure response
   * @param statusCode the status code received as a response
   * @return the message to be used on exceptions
   */
  public String createFrom(HttpRequest request, int statusCode) {
    StringBuilder stringBuilder = getBaseMessage(request);
    Optional<HttpError> httpError = getErrorByCode(statusCode);
    stringBuilder.append(httpError.map(error -> ": " + error.getErrorMessage(request) + " (" + statusCode + ")")
        .orElse(" with status code " + statusCode)).append(".");

    return stringBuilder.toString();
  }

  private StringBuilder getBaseMessage(HttpRequest request) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("HTTP ").append(request.getMethod()).append(" on resource '").append(request.getUri())
        .append("' failed");
    return stringBuilder;
  }

}
