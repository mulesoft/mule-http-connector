/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import static org.mule.runtime.api.metadata.MediaType.ANY;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpError;
import org.mule.runtime.api.exception.ErrorMessageAwareException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;

/**
 * Signals that an error occurred while validating a {@link Message}
 *
 * @since 1.0
 */
public class ResponseValidatorTypedException extends ModuleException implements ErrorMessageAwareException {

  private static final long serialVersionUID = -1610352261673523461L;

  Message errorMessage;

  public ResponseValidatorTypedException(String message, HttpError error) {
    super(message, error);
  }

  /**
   *
   * @param message the exception message
   * @param error the {@link HttpError} to raise
   * @param result the rejected data
   * @deprecated use {{@link #ResponseValidatorTypedException(String, HttpError, Message)}} instead
   */
  @Deprecated
  public ResponseValidatorTypedException(String message, HttpError error, Result<InputStream, HttpResponseAttributes> result) {
    this(message, error);
    this.errorMessage = Message.builder()
        .value(result.getOutput())
        .attributesValue(result.getAttributes().orElse(null))
        .mediaType(result.getMediaType().orElse(ANY))
        .build();
  }

  /**
   *
   * @param message the exception message
   * @param error the {@link HttpError} to raise
   * @param errorMessage
   */
  public ResponseValidatorTypedException(String message, HttpError error, Message errorMessage) {
    this(message, error);
    this.errorMessage = errorMessage;
  }

  @Override
  public Message getErrorMessage() {
    return errorMessage;
  }

}
