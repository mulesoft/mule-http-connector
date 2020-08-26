/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import io.qameta.allure.Issue;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpErrorMessageGenerator;
import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.streaming.HttpStreamingType;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.MuleConfiguration;
import org.mule.runtime.extension.api.notification.NotificationEmitter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;

public class HttpRequesterAuthConsumesPayloadTestCase {

  private HttpRequestFactory httpRequestFactory;
  private HttpErrorMessageGenerator httpErrorMessageGenerator;
  private HttpRequesterConfig config;
  private ResponseValidator responseValidator;
  private HttpRequesterRequestBuilder requestBuilder;
  private MuleContext muleContext;
  private NotificationEmitter notificationEmitter;
  private CompletionCallback<InputStream, HttpResponseAttributes> callback;
  private HttpEntity entity;
  private HttpResponse response;
  private StreamingHelper streamingHelper;
  private HttpRequest httpRequest;
  private HttpExtensionClient client;
  private MuleConfiguration muleConfiguration;
  private URI someUri;
  private String uri;
  private String textPayload;

  @Before
  public void setUp() throws Exception {
    httpRequestFactory = mock(HttpRequestFactory.class);
    httpErrorMessageGenerator = mock(HttpErrorMessageGenerator.class);

    config = mock(HttpRequesterConfig.class);
    responseValidator = mock(ResponseValidator.class);
    requestBuilder = mock(HttpRequesterRequestBuilder.class);
    muleConfiguration = mock(MuleConfiguration.class);
    muleContext = mock(MuleContext.class);
    when(muleContext.getConfiguration()).thenReturn(muleConfiguration);
    notificationEmitter = mock(NotificationEmitter.class);
    callback = mock(CompletionCallback.class);
    entity = mock(HttpEntity.class);

    response = mock(HttpResponse.class);
    when(response.getHeaders()).thenReturn(new MultiMap<>());
    when(response.getEntity()).thenReturn(entity);
    when(response.getHeaderValue(CONTENT_TYPE)).thenReturn("text/plain");

    streamingHelper = mock(StreamingHelper.class);

    textPayload = "some text payload";
    InputStream payloadInputStream = IOUtils.toInputStream(textPayload);
    when(entity.getContent()).thenReturn(payloadInputStream);
    when(entity.getLength()).thenReturn(Optional.of((long) textPayload.length()));

    when(streamingHelper.resolveCursorProvider(entity.getContent())).thenReturn(new FakeCursorProvider(payloadInputStream));

    httpRequest = mock(HttpRequest.class);
    uri = "dummyUri";
    someUri = URI.create(uri);
    when(httpRequest.getUri()).thenReturn(someUri);
    when(httpRequest.getHeaders()).thenReturn(new MultiMap<>());
    when(httpRequest.getQueryParams()).thenReturn(new MultiMap<>());

    client = mock(HttpExtensionClient.class);

    when(client.send(httpRequest, 0, false, null))
        .thenReturn(CompletableFuture.completedFuture(response));
  }

  @Test
  @Issue("MULE-18307")
  public void testDoRequestCallsStreamingHelperThenHttpResponseToResultConvertTwiceThenCallbackSuccess_WhenDoingRequestWithResendAndNoRetryWasNecessary() {
    // Given
    HttpRequestAuthentication authentication = mock(HttpRequestAuthentication.class);
    doAnswer(callNotRetryCallback()).when(authentication).retryIfShould(any(), any(), any());

    HttpResponseToResult httpResponseToResult = mock(HttpResponseToResult.class);
    HttpRequester httpRequester = new HttpRequester(httpRequestFactory, httpResponseToResult, httpErrorMessageGenerator);

    // The first time convert is called will return a result and the second time the result object will be a different instance
    Result<InputStream, HttpResponseAttributes> result1 = makeResult("One");
    Result<InputStream, HttpResponseAttributes> result2 = makeResult("Two");
    when(httpResponseToResult.convert(same(config), same(muleContext), same(response), same(entity), any(), same(someUri)))
        .thenReturn(result1, result2);

    Map<String, List<String>> injectedHeaders = new HashMap<>();
    when(httpRequestFactory.create(config, uri, "dummyMethod", null, null, null, requestBuilder,
                                   authentication, injectedHeaders)).thenReturn(httpRequest);

    // When
    boolean checkRetry = true;
    httpRequester.doRequest(client, config, uri, "dummyMethod", null, null, false, authentication,
                            0, responseValidator, null, requestBuilder, checkRetry, muleContext,
                            null, notificationEmitter, streamingHelper, callback, injectedHeaders);

    // Then
    InOrder order = inOrder(streamingHelper, httpResponseToResult, authentication, callback);
    order.verify(streamingHelper, times(1)).resolveCursorProvider(entity.getContent());
    order.verify(httpResponseToResult, times(2)).convert(same(config), same(muleContext), same(response), same(entity), any(),
                                                         same(someUri));
    order.verify(callback, times(1)).success(result2);
    order.verifyNoMoreInteractions();
  }

  @Test
  @Issue("MULE-18307")
  public void testDoRequestCallsStreamigHelperThenAuthentiationRetryIfShouldWithAMappedResultThenCallbackSuccessWithAnotherResult_WhenDoingRequestWithResendAndNoRetryWasNecesary() {
    // Given
    HttpRequestAuthentication authentication = mock(HttpRequestAuthentication.class);
    doAnswer(callNotRetryCallback()).when(authentication).retryIfShould(any(), any(), any());

    HttpResponseToResult httpResponseToResult = mock(HttpResponseToResult.class);
    HttpRequester httpRequester = new HttpRequester(httpRequestFactory, httpResponseToResult, httpErrorMessageGenerator);

    // The first time convert is called will return a result and the second time the result object will be a different instance
    Result<InputStream, HttpResponseAttributes> result1 = makeResult("One");
    Result<InputStream, HttpResponseAttributes> result2 = makeResult("Two");
    when(httpResponseToResult.convert(same(config), same(muleContext), same(response), same(entity), any(), same(someUri)))
        .thenReturn(result1, result2);

    Map<String, List<String>> injectedHeaders = new HashMap<>();
    when(httpRequestFactory.create(config, uri, "dummyMethod", null, null, null, requestBuilder,
                                   authentication, injectedHeaders)).thenReturn(httpRequest);

    boolean checkRetry = true;

    // When
    httpRequester.doRequest(client, config, uri, "dummyMethod", null, null, false, authentication,
                            0, responseValidator, null, requestBuilder, checkRetry, muleContext,
                            null, notificationEmitter, streamingHelper, callback, injectedHeaders);

    // Then
    InOrder order = inOrder(streamingHelper, httpResponseToResult, authentication, callback);
    order.verify(streamingHelper, times(1)).resolveCursorProvider(entity.getContent());
    order.verify(authentication, times(1)).retryIfShould(same(result1), any(), any());
    order.verify(callback, times(1)).success(result2);
    order.verifyNoMoreInteractions();
  }

  @Test
  @Issue("MULE-18307")
  public void testDoRequestCallsCallbackSuccessWithResultContainingUnconsumedPayloadInputStream_WhenDoingRequestWithResend()
      throws IOException {
    // Given
    HttpRequestAuthentication authentication = new PayloadConsumingHttpRequestAuthentication();

    HttpResponseToResult httpResponseToResult = new HttpResponseToResult();

    HttpRequester httpRequester = new HttpRequester(httpRequestFactory, httpResponseToResult, httpErrorMessageGenerator);

    Map<String, List<String>> injectedHeaders = new HashMap<>();
    when(httpRequestFactory.create(config, uri, "dummyMethod", null, null, null, requestBuilder,
                                   authentication, injectedHeaders)).thenReturn(httpRequest);

    boolean checkRetry = true;

    when(muleContext.getConfiguration().getDefaultEncoding()).thenReturn("UTF-8");

    // When
    httpRequester.doRequest(client, config, uri, "dummyMethod", null, null, false, authentication,
                            0, responseValidator, null, requestBuilder, checkRetry, muleContext,
                            null, notificationEmitter, streamingHelper, callback, injectedHeaders);

    // Then
    ArgumentCaptor<Result> argumentCaptor = ArgumentCaptor.forClass(Result.class);
    verify(callback, times(1)).success(argumentCaptor.capture());
    String actualPayload = IOUtils.toString((InputStream) argumentCaptor.getValue().getOutput(), UTF_8.name());
    assertThat(actualPayload, equalTo(textPayload));
  }

  private Result<InputStream, HttpResponseAttributes> makeResult(String payloadString) {
    return Result.<InputStream, HttpResponseAttributes>builder().output(IOUtils.toInputStream(payloadString)).build();
  }

  private Answer<Void> callNotRetryCallback() {
    return invocation -> {
      Object[] args = invocation.getArguments();
      Runnable notRetryCallback = (Runnable) args[2];
      notRetryCallback.run();
      return null;
    };
  }

  private static class PayloadConsumingHttpRequestAuthentication implements HttpRequestAuthentication {

    @Override
    public void authenticate(HttpRequestBuilder builder) throws MuleException {

    }

    @Override
    public boolean shouldRetry(Result<? extends Object, HttpResponseAttributes> firstAttemptResult) throws MuleException {
      return true;
    }

    @Override
    public void retryIfShould(Result<? extends Object, HttpResponseAttributes> firstAttemptResult, Runnable retryCallback,
                              Runnable notRetryCallback) {
      try {
        // Simulating how Oauth consumes the payload when using it in the refreshTokenWhenExpression
        String text = IOUtils.toString((InputStream) firstAttemptResult.getOutput(), UTF_8.name());
        notRetryCallback.run();
      } catch (IOException e) {
        throw new RuntimeException("Error when consuming the payload in retryIfShould");
      }
    }
  }

  private static class FakeCursorStream extends CursorStream implements Cursor {

    private final InputStream payload;
    private final CursorProvider<FakeCursorStream> cursorProvider;

    FakeCursorStream(InputStream payload, CursorProvider<FakeCursorStream> cursorProvider) {
      this.payload = payload;
      this.cursorProvider = cursorProvider;
    }

    @Override
    public long getPosition() {
      return 0;
    }

    @Override
    public void seek(long position) throws IOException {

    }

    @Override
    public void release() {

    }

    @Override
    public boolean isReleased() {
      return false;
    }

    @Override
    public CursorProvider getProvider() {
      return cursorProvider;
    }

    @Override
    public int read() throws IOException {
      return payload.read();
    }
  }

  private static class FakeCursorProvider implements CursorProvider<HttpRequesterAuthConsumesPayloadTestCase.FakeCursorStream> {

    String payload;

    FakeCursorProvider(InputStream is) {
      try {
        this.payload = IOUtils.toString(is, UTF_8.name());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public FakeCursorStream openCursor() {
      return new FakeCursorStream(IOUtils.toInputStream(payload), this);
    }

    @Override
    public void close() {

    }

    @Override
    public void releaseResources() {

    }

    @Override
    public boolean isClosed() {
      return false;
    }

  }
}
