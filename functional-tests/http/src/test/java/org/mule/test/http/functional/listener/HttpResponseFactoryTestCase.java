/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.extension.http.api.HttpHeaders.Values.CLOSE;
import static org.mule.extension.http.api.HttpHeaders.Values.KEEP_ALIVE;
import static org.mule.extension.http.api.streaming.HttpStreamingType.AUTO;
import static org.mule.runtime.api.metadata.DataType.INPUT_STREAM;
import static org.mule.runtime.api.metadata.DataType.STRING;
import static org.mule.sdk.api.http.HttpConstants.HttpStatus.OK;
import static org.mule.sdk.api.http.HttpHeaders.Names.CONNECTION;
import static org.mule.sdk.api.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HTTP_EXTENSION;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.extension.http.api.listener.builder.HttpListenerResponseBuilder;
import org.mule.extension.http.internal.listener.HttpResponseFactory;
import org.mule.extension.http.internal.listener.intercepting.NoInterception;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.object.CursorIteratorProvider;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.module.extension.api.http.message.HttpResponseBuilderWrapper;
import org.mule.sdk.api.http.domain.message.response.HttpResponse;
import org.mule.sdk.api.http.domain.message.response.HttpResponseBuilder;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature(HTTP_EXTENSION)
@Story("Issues")
public class HttpResponseFactoryTestCase extends AbstractMuleContextTestCase {

  private static final String EXAMPLE_STRING = "exampleString";
  private static final String WRONG_CONTENT_LENGTH = "12";
  private static final String INVALID_DATA_MSG = "Attempted to send invalid data through http response.";

  @Rule
  public ExpectedException exceptionGrabber = ExpectedException.none();

  @Test
  @Description("Verifies that the correct Content-Length is sent even when a wrong one is set as header.")
  public void testContentLengthIsOverridden() {
    HttpListenerResponseBuilder listenerResponseBuilder = mock(HttpListenerResponseBuilder.class);
    TypedValue<Object> payload = new TypedValue<>(new ByteArrayInputStream(EXAMPLE_STRING.getBytes(UTF_8)), INPUT_STREAM);
    when(listenerResponseBuilder.getBody()).thenReturn(payload);
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put(CONTENT_LENGTH, WRONG_CONTENT_LENGTH);
    when(listenerResponseBuilder.getHeaders()).thenReturn(headers);
    when(listenerResponseBuilder.getStatusCode()).thenReturn(OK.getStatusCode());
    HttpResponseFactory httpResponseBuilder = new HttpResponseFactory(AUTO, muleContext.getTransformationService(), () -> false);

    HttpResponse httpResponse =
        httpResponseBuilder.create(responseBuilder(), new NoInterception(), listenerResponseBuilder, true);
    assertThat(httpResponse.getHeaderValue(CONTENT_LENGTH), is(String.valueOf(EXAMPLE_STRING.length())));
  }

  private HttpResponseBuilder responseBuilder() {
    return new HttpResponseBuilderWrapper(org.mule.runtime.http.api.domain.message.response.HttpResponse.builder());
  }

  @Test
  @Description("Trigger exception when invalid type is sent through http response.")
  public void testCursorIteratorCausesInvalidTypeForHttpResponseError() throws RuntimeException, IOException {
    HttpListenerResponseBuilder listenerResponseBuilder = mock(HttpListenerResponseBuilder.class);
    CursorIteratorProvider cursorProvider = mock(CursorIteratorProvider.class);
    TypedValue<Object> payload = new TypedValue<>(cursorProvider, INPUT_STREAM);

    when(listenerResponseBuilder.getBody()).thenReturn(payload);
    when(listenerResponseBuilder.getHeaders()).thenReturn(new MultiMap<>());
    when(listenerResponseBuilder.getStatusCode()).thenReturn(OK.getStatusCode());

    HttpResponseFactory httpResponseBuilder = new HttpResponseFactory(AUTO, muleContext.getTransformationService(), () -> false);

    exceptionGrabber.expect(RuntimeException.class);
    exceptionGrabber.expectMessage(INVALID_DATA_MSG);
    httpResponseBuilder.create(responseBuilder(), new NoInterception(), listenerResponseBuilder, true);
  }

  @Issue("MULE-18396")
  @Test
  @Description("Forces connection close header honoring the constructor supplier.")
  public void forceConnectionCloseHeaderWhenNotPresent() {
    checkForceConnectionCloseHeader(true, null);
  }

  @Issue("MULE-18396")
  @Test
  @Description("Forces connection close header honoring the constructor supplier.")
  public void forceConnectionCloseHeaderWhenPresent() {
    checkForceConnectionCloseHeader(true, KEEP_ALIVE);
  }

  @Issue("MULE-18396")
  @Test
  @Description("Doesn't force connection close header honoring the constructor supplier.")
  public void doesNotForceConnectionCloseHeaderWhenNotPresent() {
    checkForceConnectionCloseHeader(false, null);
  }

  @Issue("MULE-18396")
  @Test
  @Description("Doesn't force connection close header honoring the constructor supplier.")
  public void doesNotForceConnectionCloseHeaderWhenPresent() {
    checkForceConnectionCloseHeader(false, KEEP_ALIVE);
  }

  private void checkForceConnectionCloseHeader(final boolean shouldForceConnectionClose, String defaultValue) {
    HttpListenerResponseBuilder listenerResponseBuilder = mock(HttpListenerResponseBuilder.class);
    TypedValue<Object> payload = new TypedValue<>(EXAMPLE_STRING, STRING);
    when(listenerResponseBuilder.getBody()).thenReturn(payload);
    when(listenerResponseBuilder.getStatusCode()).thenReturn(OK.getStatusCode());

    MultiMap<String, String> headers = new MultiMap<>();
    if (defaultValue != null) {
      headers.put(CONNECTION, defaultValue);
    }
    when(listenerResponseBuilder.getHeaders()).thenReturn(headers);

    HttpResponseFactory httpResponseBuilder =
        new HttpResponseFactory(AUTO, muleContext.getTransformationService(), () -> shouldForceConnectionClose);

    HttpResponse httpResponseWithHeader =
        httpResponseBuilder.create(responseBuilder(), new NoInterception(), listenerResponseBuilder, true);

    if (shouldForceConnectionClose) {
      assertThat(httpResponseWithHeader.getHeaderValue(CONNECTION), is(CLOSE));
    } else {
      assertThat(httpResponseWithHeader.getHeaderValue(CONNECTION), is(defaultValue));
    }
  }
}
