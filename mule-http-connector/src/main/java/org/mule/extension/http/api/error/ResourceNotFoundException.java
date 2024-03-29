/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.error;

import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_FOUND;

import org.mule.extension.http.api.HttpListenerResponseAttributes;
import org.mule.runtime.api.exception.ErrorMessageAwareException;
import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * Thrown when a static file is requested but not found, associated with a 404 status code.
 *
 * @since 1.0
 */
public class ResourceNotFoundException extends ModuleException implements ErrorMessageAwareException {

  private static final long serialVersionUID = 3137973689262542839L;

  public <T extends Enum<T>> ResourceNotFoundException(ErrorTypeDefinition<T> errorTypeDefinition,
                                                       I18nMessage message) {
    super(message, errorTypeDefinition);
  }

  public <T extends Enum<T>> ResourceNotFoundException(Exception exception,
                                                       ErrorTypeDefinition<T> errorTypeDefinition,
                                                       I18nMessage message) {
    super(message, errorTypeDefinition, exception);
  }

  @Override
  public Message getErrorMessage() {
    return Message.builder()
        .nullValue()
        .attributesValue(new HttpListenerResponseAttributes(NOT_FOUND.getStatusCode(), NOT_FOUND.getReasonPhrase(),
                                                            emptyMultiMap()))
        .build();
  }

}
