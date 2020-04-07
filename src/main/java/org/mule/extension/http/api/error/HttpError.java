/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.error;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.ofNullable;
import static org.mule.extension.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.getStatusByCode;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents an error that can happen in an HTTP operation.
 *
 * @since 1.0
 */
public enum HttpError implements ErrorTypeDefinition<HttpError> {

  PARSING,

  TIMEOUT,

  SECURITY(MuleErrors.SECURITY),

  CLIENT_SECURITY(MuleErrors.CLIENT_SECURITY),

  SERVER_SECURITY(MuleErrors.SERVER_SECURITY),

  TRANSFORMATION(MuleErrors.TRANSFORMATION),

  CONNECTIVITY(MuleErrors.CONNECTIVITY),

  BAD_REQUEST,

  BASIC_AUTHENTICATION(SERVER_SECURITY),

  UNAUTHORIZED(CLIENT_SECURITY),

  FORBIDDEN(CLIENT_SECURITY),

  NOT_FOUND,

  METHOD_NOT_ALLOWED,

  NOT_ACCEPTABLE,

  UNSUPPORTED_MEDIA_TYPE(
      request -> "media type " + request.getHeaderValue(CONTENT_TYPE) + " not supported"),

  TOO_MANY_REQUESTS,

  INTERNAL_SERVER_ERROR,

  SERVICE_UNAVAILABLE,

  BAD_GATEWAY,

  GATEWAY_TIMEOUT;

  private static Set<ErrorTypeDefinition> httpRequestOperationErrors;

  static {
    final Set<ErrorTypeDefinition> errors = new HashSet<>();

    errors.add(PARSING);
    errors.add(TIMEOUT);
    errors.add(SECURITY);
    errors.add(CLIENT_SECURITY);
    errors.add(CONNECTIVITY);
    errors.add(BAD_REQUEST);
    errors.add(FORBIDDEN);
    errors.add(UNAUTHORIZED);
    errors.add(METHOD_NOT_ALLOWED);
    errors.add(TOO_MANY_REQUESTS);
    errors.add(NOT_FOUND);
    errors.add(UNSUPPORTED_MEDIA_TYPE);
    errors.add(NOT_ACCEPTABLE);
    errors.add(INTERNAL_SERVER_ERROR);
    errors.add(SERVICE_UNAVAILABLE);
    errors.add(BAD_GATEWAY);
    errors.add(GATEWAY_TIMEOUT);

    httpRequestOperationErrors = unmodifiableSet(errors);
  }

  private ErrorTypeDefinition<?> parentErrorType;

  private Function<HttpRequest, String> errorMessageFunction;

  HttpError() {
    String message = this.name().replace("_", " ").toLowerCase();
    errorMessageFunction = httpRequest -> message;
  }

  HttpError(ErrorTypeDefinition<?> parentErrorType) {
    this();
    this.parentErrorType = parentErrorType;
  }

  HttpError(Function<HttpRequest, String> errorMessageFunction) {
    this();
    this.errorMessageFunction = errorMessageFunction;
  }

  HttpError(ErrorTypeDefinition<?> parentErrorType, Function<HttpRequest, String> errorMessageFunction) {
    this.parentErrorType = parentErrorType;
    this.errorMessageFunction = errorMessageFunction;
  }

  @Override
  public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
    return ofNullable(parentErrorType);
  }

  /**
   * Returns the {@link HttpError} corresponding to a given status code. A match is found if there's an {@link HttpError} with
   * the same name as the status code's corresponding {@link HttpStatus}.
   *
   * @param statusCode the HTTP status code to search for
   * @return an {@link Optional} with the
   */
  public static Optional<HttpError> getErrorByCode(int statusCode) {
    HttpError error = null;
    HttpStatus status = getStatusByCode(statusCode);
    if (status != null) {
      error = stream(HttpError.values())
          .filter(httpError -> httpError.name().equals(status.name()))
          .findFirst()
          .orElse(null);
    }
    return ofNullable(error);
  }

  /**
   * Returns an {@link HttpStatus} corresponding to a given {@link HttpError}. A match is found if there's an {@link HttpStatus}
   * with the same name as the {@link HttpError}.
   *
   * @param error the {@link HttpError} to match
   * @return
   */
  public static Optional<HttpStatus> getHttpStatus(HttpError error) {
    HttpStatus result = null;
    for (HttpStatus status : HttpStatus.values()) {
      if (error.name().equals(status.name())) {
        result = status;
      }
    }
    return ofNullable(result);
  }

  public static Set<ErrorTypeDefinition> getHttpRequestOperationErrors() {
    return httpRequestOperationErrors;
  }

  /**
   * Returns the custom error message for this {@link HttpError} based on the {@link HttpRequest} that triggered it.
   *
   * @param request the {@link HttpRequest} that caused the error
   * @return the custom error message associated to the error
   */
  public String getErrorMessage(HttpRequest request) {
    return errorMessageFunction.apply(request);
  }

}
