/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.api.HttpConnectorConstants.RESPONSE;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

/**
 * Groups parameters regarding response validation
 *
 * @since 1.0
 */
public final class ResponseValidationSettings {

  /**
   * Configures error handling of the response.
   */
  @Parameter
  @ConfigOverride
  @Placement(tab = RESPONSE)
  private ResponseValidator responseValidator;

  public ResponseValidator getResponseValidator() {
    return responseValidator;
  }
}
