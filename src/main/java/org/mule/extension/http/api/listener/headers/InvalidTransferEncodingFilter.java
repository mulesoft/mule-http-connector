/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import static java.util.Arrays.asList;
import static org.mule.extension.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import org.mule.runtime.api.util.MultiMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Filter utilized to check the "Transfer-Encoding" header value in the request. All the valid values are specified in the
 * following RFCs:
 * https://www.rfc-editor.org/rfc/rfc7230.html
 * https://www.rfc-editor.org/rfc/rfc2616.html
 */
public class InvalidTransferEncodingFilter implements HttpHeadersFilter {

  private static final Set<String> validTransferEncodings =
      new HashSet<>(asList("chunked", "compress", "deflate", "gzip", "identity"));

  @Override
  public MultiMap<String, String> filter(MultiMap<String, String> headers) throws HttpHeaderError {
    if (headers.getAll(TRANSFER_ENCODING).stream().map(String::toLowerCase)
        .anyMatch(headerValue -> !validTransferEncodings.contains(headerValue))) {
      throw new HttpHeaderError(TRANSFER_ENCODING);
    }
    return headers;
  }
}
