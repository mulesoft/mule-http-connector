/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.validator;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.io.InputStream;

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
   * @throws ResponseValidatorTypedException if the message is not considered valid and the response validator throws a
   *         {@link org.mule.runtime.extension.api.exception.ModuleException}
   * @throws ResponseValidatorException if the message is not considered valid and the response validator does not relates to an
   *         specific error type.
   * @deprecated use {@link #validate(Result, HttpRequest, StreamingHelper)} instead
   */
  void validate(Result<InputStream, HttpResponseAttributes> result, HttpRequest request);

  /**
   * Validates whether an HTTP response result should be accepted or not, failing in that case. By default this will behave as
   * {@link #validate(Result, HttpRequest)}, but implementations should use the {@code streamingHelper} to guarantee management of
   * streams.
   *
   * @param result the message to validate
   * @param request the request that cause the {@code result}
   * @param streamingHelper helper component that allows managing the {@code result} stream
   * @throws ResponseValidatorTypedException if the message is not considered valid and the response validator throws a
   *         {@link org.mule.runtime.extension.api.exception.ModuleException}
   * @throws ResponseValidatorException if the message is not considered valid and the response validator does not relates to an
   *         specific error type.
   */
  default void validate(Result<InputStream, HttpResponseAttributes> result, HttpRequest request,
                        StreamingHelper streamingHelper) {
    validate(result, request);
  }

  default boolean mayConsumeBody() {
    return false;
  }
}
