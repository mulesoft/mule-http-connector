/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.client.fluent.Request.Get;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.internal.listener.HttpListener.HTTP_NAMESPACE;
import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;
import static org.mule.runtime.api.metadata.MediaType.TEXT;
import static org.mule.runtime.core.api.exception.Errors.ComponentIdentifiers.Handleable.EXPRESSION;
import static org.mule.runtime.core.api.exception.Errors.ComponentIdentifiers.Handleable.SECURITY;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.FORBIDDEN;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.METHOD_NOT_ALLOWED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_ACCEPTABLE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_FOUND;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.TOO_MANY_REQUESTS;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.UNAUTHORIZED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.ERRORS;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.ERROR_HANDLING;
import static org.mule.test.http.functional.matcher.HttpResponseContentStringMatcher.body;
import static org.mule.test.http.functional.matcher.HttpResponseHeaderStringMatcher.header;
import static org.mule.test.http.functional.matcher.HttpResponseReasonPhraseMatcher.hasReasonPhrase;
import static org.mule.test.http.functional.matcher.HttpResponseStatusCodeMatcher.hasStatusCode;
import org.mule.extension.http.api.HttpListenerResponseAttributes;
import org.mule.functional.junit4.rules.HttpServerRule;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.exception.ErrorTypeRepository;
import org.mule.runtime.core.api.exception.TypedException;
import org.mule.runtime.core.api.exception.WrapperErrorMessageAwareException;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;

import io.qameta.allure.Feature;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Response;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
@Stories({@Story(ERROR_HANDLING), @Story(ERRORS)})
public class HttpListenerErrorInterpretationTestCase extends AbstractHttpTestCase {

  public static final String HEADER_NAME = "X-Custom";
  public static final String HEADER_VALUE = "custom";
  @Rule
  public DynamicPort port = new DynamicPort("port");
  @Rule
  public HttpServerRule serverRule = new HttpServerRule("port2");

  private static final String OOPS = "Oops";
  private static final String ERROR = "Error";
  private static ErrorType errorToThrow;
  private static Object attributesToSend;

  private ErrorTypeRepository errorTypeRepository;

  @Override
  protected String getConfigFile() {
    return "http-listener-error-interpretation-config.xml";
  }

  @Before
  public void setUp() {
    errorTypeRepository = muleContext.getErrorTypeRepository();
  }

  @Test
  public void unknownErrorCauses500() throws Exception {
    verifyStatus(errorTypeRepository.lookupErrorType(EXPRESSION).get());
  }

  @Test
  public void badRequestErrorCauses500() throws Exception {
    verifyStatusIsKnown(BAD_REQUEST);
  }

  @Test
  public void unauthorizedErrorCauses500() throws Exception {
    verifyStatusIsKnown(UNAUTHORIZED);
  }

  @Test
  public void forbiddenErrorCauses500() throws Exception {
    verifyStatusIsKnown(FORBIDDEN);
  }

  @Test
  public void notFoundErrorCauses500() throws Exception {
    verifyStatusIsKnown(NOT_FOUND);
  }

  @Test
  public void methodNotAllowedErrorCauses500() throws Exception {
    verifyStatusIsKnown(METHOD_NOT_ALLOWED);
  }

  @Test
  public void notAcceptableErrorCauses500() throws Exception {
    verifyStatusIsKnown(NOT_ACCEPTABLE);
  }

  @Test
  public void unsupportedMediaTypeErrorCauses500() throws Exception {
    verifyStatusIsKnown(UNSUPPORTED_MEDIA_TYPE);
  }

  @Test
  public void tooManyRequestsErrorCauses500() throws Exception {
    verifyStatusIsKnown(TOO_MANY_REQUESTS);
  }

  @Test
  public void internalServerErrorErrorCauses500() throws Exception {
    verifyStatusIsKnown(INTERNAL_SERVER_ERROR);
  }

  @Test
  public void serviceUnavailablesErrorCauses500() throws Exception {
    verifyStatusIsKnown(SERVICE_UNAVAILABLE);
  }

  @Test
  public void unknownErrorTypeWithMessageIsNotConsidered() throws Exception {
    final HttpResponse httpResponse = getAndVerifyResponseFromErrorWithCustomMessage(EXPRESSION, FORBIDDEN, INTERNAL_SERVER_ERROR,
                                                                                     OOPS);
    assertThat(httpResponse.getFirstHeader(HEADER_NAME), is(nullValue()));
  }

  @Test
  public void knownErrorTypeWithMessageIsConsidered() throws Exception {
    final HttpResponse httpResponse = getAndVerifyResponseFromErrorWithCustomMessage(SECURITY, UNAUTHORIZED, UNAUTHORIZED, OOPS);
    assertThat(httpResponse, header(HEADER_NAME, is(HEADER_VALUE)));
  }

  @Test
  public void requestErrorIsNotInterpretedIfNotSelected() throws Exception {
    verifyResponseFromRequestError("requestError", INTERNAL_SERVER_ERROR, containsString("failed"));
  }

  @Test
  public void errorResponseOverridesThrownErrors() throws Exception {
    verifyErrorResponseOverride("errorResponse");
  }

  @Test
  public void errorResponseOverridesThrownErrorsWithMessage() throws Exception {
    verifyErrorResponseOverride("errorResponseWithMessage");
  }

  void verifyStatusIsKnown(HttpStatus status) throws IOException {
    ErrorType statusError = errorTypeRepository.lookupErrorType(buildFromStringRepresentation(getErrorName(status))).get();
    verifyStatus(statusError);
  }

  void verifyStatus(ErrorType errorType) throws IOException {
    errorToThrow = errorType;
    final Response response = Get(getUrl("error")).execute();
    final HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse, hasStatusCode(INTERNAL_SERVER_ERROR.getStatusCode()));
    assertThat(httpResponse, hasReasonPhrase(INTERNAL_SERVER_ERROR.getReasonPhrase()));
    assertThat(httpResponse, header(CONTENT_TYPE, is(TEXT.withCharset(UTF_8).toRfcString())));
    assertThat(httpResponse, body(is(OOPS)));
  }

  HttpResponse getAndVerifyResponseFromErrorWithCustomMessage(ComponentIdentifier errorIdentifier, HttpStatus customStatus,
                                                              HttpStatus expectedStatus, String expectedBody)
      throws IOException {
    errorToThrow = errorTypeRepository.lookupErrorType(errorIdentifier).get();
    createListenerResponseAttributes(customStatus);
    final Response response = Get(getUrl("errorMessage")).execute();
    final HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse, hasStatusCode(expectedStatus.getStatusCode()));
    assertThat(httpResponse, hasReasonPhrase(expectedStatus.getReasonPhrase()));
    assertThat(httpResponse, body(is(expectedBody)));
    return httpResponse;
  }

  void createListenerResponseAttributes(HttpStatus status) {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put(HEADER_NAME, HEADER_VALUE);
    attributesToSend = new HttpListenerResponseAttributes(status.getStatusCode(), status.getReasonPhrase(), headers);
  }

  void verifyResponseFromRequestError(String path, HttpStatus expectedStatus, Matcher<String> expectedBody) throws Exception {
    serverRule.getSimpleHttpServer().setResponseStatusCode(201);
    final Response response = Get(getUrl(path)).execute();
    final HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse, hasStatusCode(expectedStatus.getStatusCode()));
    assertThat(httpResponse, hasReasonPhrase(expectedStatus.getReasonPhrase()));
    assertThat(httpResponse, body(expectedBody));
  }

  void verifyErrorResponseOverride(String path) throws IOException {
    errorToThrow = errorTypeRepository.lookupErrorType(SECURITY).get();
    createListenerResponseAttributes(NOT_FOUND);
    final Response response = Get(getUrl(path)).execute();
    final HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse, hasStatusCode(FORBIDDEN.getStatusCode()));
    assertThat(httpResponse, hasReasonPhrase(FORBIDDEN.getReasonPhrase()));
    assertThat(httpResponse, body(is("Cant see this")));
    assertThat(httpResponse, header("XX-Custom", is("Xcustom")));
  }

  private String getErrorName(HttpStatus status) {
    return String.format("%s:%s", HTTP_NAMESPACE, status.name());
  }

  private String getUrl(String path) {
    return String.format("http://localhost:%s/%s", port.getValue(), path);
  }

  public static class ErrorException extends TypedException {

    public ErrorException() {
      super(new IOException(OOPS), errorToThrow);
    }

  }

  public static class ErrorMessageException extends TypedException {

    public ErrorMessageException() {
      super(new WrapperErrorMessageAwareException(Message.builder()
          .value(ERROR)
          .attributesValue(attributesToSend)
          .build(), new IOException(OOPS)), errorToThrow);
    }

  }

}
