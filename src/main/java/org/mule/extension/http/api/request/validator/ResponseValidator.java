/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.service.http.api.domain.message.request.HttpRequest;

/**
 * Configures error handling of the response.
 *
 * @since 1.0
 */
public interface ResponseValidator {

  /**
   * Validates whether an HTTP response result should be accepted or not, failing in that case.
   *
   * @param result the message to validate
   * @param request the request that cause the {@code result}
   * @throws ResponseValidatorException if the message is not considered valid.
   */
  void validate(Result<Object, HttpResponseAttributes> result, HttpRequest request) throws ResponseValidatorException;

}
