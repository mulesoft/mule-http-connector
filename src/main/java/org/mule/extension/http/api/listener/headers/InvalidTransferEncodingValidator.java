/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Locale.ROOT;
import static org.mule.extension.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;

import org.mule.runtime.api.util.MultiMap;

import java.util.Collection;
import java.util.HashSet;

/**
 * Validator utilized to check the "Transfer-Encoding" header value in the request. All the valid values are specified in
 * <a href="https://www.rfc-editor.org/rfc/rfc7230.html">RFC-7230</a> and
 * <a href="https://www.rfc-editor.org/rfc/rfc2616.html">RFC-2616</a>
 */
public class InvalidTransferEncodingValidator implements HttpHeadersValidator {

  private static final Collection<String> validTransferEncodings =
      new HashSet<>(asList("chunked", "compress", "deflate", "gzip", "identity"));

  /**
   * Checks that 'Transfer-Encoding' header value has a valid value.
   *
   * @param headers All the headers in the request.
   * @throws HttpHeadersException with a 400 (Bad Request) if the 'Transfer-Encoding' value is invalid.
   */
  @Override
  public void validateHeaders(MultiMap<String, String> headers) throws HttpHeadersException {
    if (headers.getAll(TRANSFER_ENCODING).stream().map(s -> s.toLowerCase(ROOT))
        .anyMatch(headerValue -> !validTransferEncodings.contains(headerValue))) {
      throw new HttpHeadersException(format("'%s' header has an invalid value", TRANSFER_ENCODING), BAD_REQUEST);
    }
  }
}
