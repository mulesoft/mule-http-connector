/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.headers;

import io.qameta.allure.Issue;
import io.qameta.allure.Story;
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

@Story("Invalid Transfer Encoding")
@Issue("HTTPC-149")
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

  @Test
  public void multipleValidValuesInTheSameHeader() {
    assertThatTransferEncodingValueIsValid("deflate, chunked");
  }

  @Test
  public void multipleValidValuesInSeparatedHeaders() {
    assertThatTransferEncodingValueIsValid("chunked", "deflate");
  }

  @Test
  public void chunkedWithQuotesIsInvalid() throws HttpHeadersException {
    assertThatTransferEncodingValueIsInvalid("'chunked'");
  }

  @Test
  public void validAndInvalidInTheSameHeaderValidFirst() throws HttpHeadersException {
    assertThatTransferEncodingValueIsInvalid("deflate, 'chunked'");
  }

  @Test
  public void validAndInvalidInTheSameHeaderInvalidFirst() throws HttpHeadersException {
    assertThatTransferEncodingValueIsInvalid("'chunked', deflate");
  }

  @Test
  public void validAndInvalidInTheSeparatedHeadersInvalidFirst() throws HttpHeadersException {
    assertThatTransferEncodingValueIsInvalid("'chunked'", "deflate");
  }

  @Test
  public void validAndInvalidInTheSeparatedHeadersValidFirst() throws HttpHeadersException {
    assertThatTransferEncodingValueIsInvalid("deflate", "'chunked'");
  }

  @Test
  public void obviouslyInvalidValue() throws HttpHeadersException {
    assertThatTransferEncodingValueIsInvalid("thisIsNotValid");
  }

  private static void assertThatTransferEncodingValueIsValid(String... transferEncodings) {
    MultiMap<String, String> headers = new MultiMap<>();
    for (String transferEncoding : transferEncodings) {
      headers.put(TRANSFER_ENCODING, transferEncoding);
    }

    HttpHeadersValidator validator = new InvalidTransferEncodingValidator();
    try {
      validator.validateHeaders(headers);
    } catch (HttpHeadersException e) {
      fail("chunked has been considered invalid");
    }
  }

  private void assertThatTransferEncodingValueIsInvalid(String... transferEncodings) throws HttpHeadersException {
    MultiMap<String, String> headers = new MultiMap<>();
    for (String transferEncoding : transferEncodings) {
      headers.put(TRANSFER_ENCODING, transferEncoding);
    }

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
