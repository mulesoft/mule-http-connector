/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import static java.lang.String.format;
import static org.mule.extension.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.util.MultiMap;
import org.slf4j.Logger;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Validator utilized to check the "Transfer-Encoding" header value in the request. All the valid values are specified in
 * <a href="https://www.rfc-editor.org/rfc/rfc7230.html">RFC-7230</a> and
 * <a href="https://www.rfc-editor.org/rfc/rfc2616.html">RFC-2616</a>
 *
 * @since 1.6.0
 */
public class InvalidTransferEncodingValidator implements HttpHeadersValidator {

  private static final Logger LOGGER = getLogger(InvalidTransferEncodingValidator.class);
  private static final String TRANSFER_ENCODING_LOWERCASE = TRANSFER_ENCODING.toLowerCase(Locale.ROOT);
  private static final int ERRORS_LIMIT_TO_PRINT_WARNING = 2 << 20;
  private final boolean throwException;
  private final AtomicInteger errorsFound = new AtomicInteger();

  /**
   * @param throwException if {@code true}, the errors will be reported as an exception. Otherwise, the errors will be
   *                       logged. One of each {@link ERRORS_LIMIT_TO_PRINT_WARNING} errors will be printed as a WARN log,
   *                       and the others as TRACE, in order to avoid log flooding.
   */
  public InvalidTransferEncodingValidator(boolean throwException) {
    this.throwException = throwException;
  }

  /**
   * Checks that 'Transfer-Encoding' header value has a valid value.
   *
   * @param headers all the headers in the request.
   * @throws HttpHeadersException with a 400 (Bad Request) if the 'Transfer-Encoding' value is invalid.
   */
  @Override
  public void validateHeaders(MultiMap<String, String> headers) throws HttpHeadersException {
    List<String> allTransferEncodings = headers.getAll(TRANSFER_ENCODING_LOWERCASE);
    int numberOfHeaders = allTransferEncodings.size();

    // avoid creating the implicit iterator in order to optimize performance
    if (numberOfHeaders == 0) {
      return;
    }
    if (numberOfHeaders == 1) {
      if (!isValidTransferEncodingHeader(allTransferEncodings.get(0))) {
        reportError();
      }
      return;
    }

    for (String header : allTransferEncodings) {
      if (!isValidTransferEncodingHeader(header)) {
        reportError();
      }
    }
  }

  private void reportError() throws HttpHeadersException {
    if (throwException) {
      throw new HttpHeadersException(format("'%s' header has an invalid value", TRANSFER_ENCODING), BAD_REQUEST);
    } else {
      if (errorsFound.getAndIncrement() % ERRORS_LIMIT_TO_PRINT_WARNING == 0) {
        LOGGER.warn("'{}' header has an invalid value", TRANSFER_ENCODING);
      } else {
        LOGGER.trace("'{}' header has an invalid value", TRANSFER_ENCODING);
      }
    }
  }

  private static boolean isValidTransferEncodingHeader(String headerValue) {
    if ("chunked".equals(headerValue)) {
      // optimize the common case
      return true;
    }

    if (headerValue.contains(",")) {
      for (String singleTransferEncoding : headerValue.split(",")) {
        if (!isSingleHeaderValid(singleTransferEncoding)) {
          return false;
        }
      }
      return true;
    } else {
      return isSingleHeaderValid(headerValue);
    }
  }

  private static boolean isSingleHeaderValid(String transferEncoding) {
    String trimmed = trimIfNeeded(transferEncoding);
    int size = trimmed.length();
    switch (size) {
      case 7:
        return ("chunked".equals(toLowerIfNeeded(trimmed)) || "deflate".equals(toLowerIfNeeded(trimmed)));
      case 8:
        return ("compress".equals(toLowerIfNeeded(trimmed)) || "identity".equals(toLowerIfNeeded(trimmed)));
      case 4:
        return ("gzip".equals(toLowerIfNeeded(trimmed)));
      default:
        return false;
    }
  }

  private static String toLowerIfNeeded(String input) {
    if (isStringLowerCase(input)) {
      return input;
    } else {
      return input.toLowerCase(Locale.ROOT);
    }
  }

  private static boolean isStringLowerCase(CharSequence input) {
    int size = input.length();
    for (int i = 0; i < size; ++i) {
      char c = input.charAt(i);
      if (c < 'a' || c > 'z') {
        return false;
      }
    }
    return true;
  }

  private static String trimIfNeeded(String input) {
    // trim returns same instance if no trim is needed.
    return input.trim();
  }
}
