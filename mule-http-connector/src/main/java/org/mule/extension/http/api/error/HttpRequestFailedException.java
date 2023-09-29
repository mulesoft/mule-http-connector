/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.error;

import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * Exception thrown when there is a generic HTTP request failure.
 *
 * @since 1.0
 */
public class HttpRequestFailedException extends ModuleException {

  private static final long serialVersionUID = 2516645632512321036L;

  public HttpRequestFailedException(I18nMessage message, Throwable throwable, ErrorTypeDefinition<HttpError> errorType) {
    super(message, errorType, throwable);
  }

}
