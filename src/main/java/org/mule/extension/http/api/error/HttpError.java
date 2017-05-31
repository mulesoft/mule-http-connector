/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.error;

import static java.util.Optional.ofNullable;
import static org.mule.extension.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.getStatusByCode;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import com.google.common.collect.ImmutableSet;

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
      request -> "media type " + request.getHeaderValueIgnoreCase(CONTENT_TYPE) + " not supported"),

  TOO_MANY_REQUESTS,

  INTERNAL_SERVER_ERROR,

  SERVICE_UNAVAILABLE;

  private ErrorTypeDefinition<?> parentErrorType;

  private Function<HttpRequest, String> errorMessageFunction;

  HttpError() {
    errorMessageFunction = httpRequest -> this.name().replace("_", " ").toLowerCase();
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
      try {
        error = HttpError.valueOf(status.name());
      } catch (Throwable e) {
        // Do nothing
      }
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
    return ImmutableSet.<ErrorTypeDefinition>builder()
        .add(PARSING).add(TIMEOUT).add(SECURITY).add(CLIENT_SECURITY).add(CONNECTIVITY).add(BAD_REQUEST).add(FORBIDDEN)
        .add(UNAUTHORIZED).add(METHOD_NOT_ALLOWED).add(TOO_MANY_REQUESTS)
        .add(NOT_FOUND).add(UNSUPPORTED_MEDIA_TYPE).add(NOT_ACCEPTABLE).add(INTERNAL_SERVER_ERROR).add(SERVICE_UNAVAILABLE)
        .build();
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
