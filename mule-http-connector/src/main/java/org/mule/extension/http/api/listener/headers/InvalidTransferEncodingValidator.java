/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import static java.lang.String.format;
import static org.mule.extension.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.sdk.api.http.HttpConstants.HttpStatus.BAD_REQUEST;
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

  // Valid Transfer-Encoding values given the RFCs.
  private static final String CHUNKED_LOWER_CASE = "chunked";
  private static final String DEFLATE_LOWER_CASE = "deflate";
  private static final String COMPRESS_LOWER_CASE = "compress";
  private static final String IDENTITY_LOWER_CASE = "identity";
  private static final String GZIP_LOWER_CASE = "gzip";

  // Preset these values to optimize performance.
  private static final int CHUNKED_AND_DEFLATE_LENGTH = 7;
  private static final int COMPRESS_AND_IDENTITY_LENGTH = 8;
  private static final int GZIP_LENGTH = 4;

  private final boolean throwException;
  private final AtomicInteger errorsFound = new AtomicInteger();

  /**
   * @param throwException if {@code true}, the errors will be reported as an exception. Otherwise, the errors will be logged. One
   *                       of each {@link ERRORS_LIMIT_TO_PRINT_WARNING} errors will be printed as a WARN log, and the others as
   *                       TRACE, in order to avoid log flooding.
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
    int numberOfTransferEncodings = allTransferEncodings.size();

    // avoid creating the implicit iterator in order to optimize performance
    if (numberOfTransferEncodings == 0) {
      return;
    }
    if (numberOfTransferEncodings == 1) {
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
    if (CHUNKED_LOWER_CASE.equals(headerValue)) {
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
    final String trimmed = transferEncoding.trim();
    int size = trimmed.length();
    switch (size) {
      case CHUNKED_AND_DEFLATE_LENGTH:
        return CHUNKED_LOWER_CASE.equalsIgnoreCase(trimmed) || DEFLATE_LOWER_CASE.equalsIgnoreCase(trimmed);
      case COMPRESS_AND_IDENTITY_LENGTH:
        return COMPRESS_LOWER_CASE.equalsIgnoreCase(trimmed) || IDENTITY_LOWER_CASE.equalsIgnoreCase(trimmed);
      case GZIP_LENGTH:
        return GZIP_LOWER_CASE.equalsIgnoreCase(trimmed);
      default:
        return false;
    }
  }
}
