/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.filter;

import org.mule.runtime.api.exception.ErrorMessageAwareException;
import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.security.SecurityContext;
import org.mule.runtime.api.security.UnauthorisedException;

public class BasicUnauthorisedException extends UnauthorisedException implements ErrorMessageAwareException {

  private static final long serialVersionUID = -5707279260743243251L;

  private final Message errorMessage;

  public BasicUnauthorisedException(I18nMessage message, Message errorMessage) {
    super(message);
    this.errorMessage = errorMessage;
  }

  public BasicUnauthorisedException(I18nMessage message, Throwable cause, Message errorMessage) {
    super(message, cause);
    this.errorMessage = errorMessage;
  }

  public BasicUnauthorisedException(SecurityContext context, String filter, String connector, Message errorMessage) {
    super(context, filter, connector);
    this.errorMessage = errorMessage;
  }

  @Override
  public Message getErrorMessage() {
    return errorMessage;
  }

}
