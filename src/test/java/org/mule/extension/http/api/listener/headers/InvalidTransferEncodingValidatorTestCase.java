/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.junit.rules.ExpectedException.none;
import static org.mule.extension.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;

public class InvalidTransferEncodingValidatorTestCase {

  @Rule
  public ExpectedException exception = none();

  @Test
  public void chunkedIsAValidTransferEncoding() {
    assertThatTransferEncodingValueIsValid("chunked");
  }

  @Test
  public void validatorIsCaseInsensitive() {
    assertThatTransferEncodingValueIsValid("cHuNkEd");
  }

  @Test
  public void otherValidHeaders() {
    for (String transferEncoding : asList("compress", "deflate", "gzip", "identity")) {
      assertThatTransferEncodingValueIsValid(transferEncoding);
    }
  }

  private static void assertThatTransferEncodingValueIsValid(String transferEncoding) {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put(TRANSFER_ENCODING, transferEncoding);

    HttpHeadersValidator validator = new InvalidTransferEncodingValidator();
    try {
      validator.validateHeaders(headers);
    } catch (HttpHeadersException e) {
      fail("chunked has been considered invalid");
    }
  }

  @Test
  public void chunkedWithQuotesIsInvalid() throws HttpHeadersException {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put(TRANSFER_ENCODING, "'chunked'");

    exception.expect(HttpHeadersException.class);
    exception.expectMessage("'Transfer-Encoding' header has an invalid value");
    exception.expect(new HasStatusCode(BAD_REQUEST));
    HttpHeadersValidator validator = new InvalidTransferEncodingValidator();
    validator.validateHeaders(headers);
  }

  @Test
  public void obviouslyInvalidValue() throws HttpHeadersException {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put(TRANSFER_ENCODING, "thisIsNotValid");

    exception.expect(HttpHeadersException.class);
    exception.expectMessage("'Transfer-Encoding' header has an invalid value");
    exception.expect(new HasStatusCode(BAD_REQUEST));
    HttpHeadersValidator validator = new InvalidTransferEncodingValidator();
    validator.validateHeaders(headers);
  }

  private static class HasStatusCode extends BaseMatcher<HttpHeadersException> {

    private final HttpStatus expectedStatus;

    HasStatusCode(final HttpStatus expectedStatus) {
      this.expectedStatus = expectedStatus;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("Incorrect status code. Expected was: '%s', but found: '%s'");
    }

    @Override
    public boolean matches(Object item) {
      if (!(item instanceof HttpHeadersException)) {
        throw new IllegalArgumentException("Exception is expected to be an instance of HttpHeadersException");
      }
      HttpHeadersException exception = (HttpHeadersException) item;
      return exception.getStatusCode() == expectedStatus;
    }

    @Override
    public String toString() {
      return "HasStatusCode " + expectedStatus;
    }
  }
}
