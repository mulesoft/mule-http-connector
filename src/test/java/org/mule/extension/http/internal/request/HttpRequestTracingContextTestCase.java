/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.http.internal.request;

import static org.mule.runtime.api.metadata.DataType.BYTE_ARRAY;
import static org.mule.runtime.api.metadata.DataType.STRING;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.streaming.HttpStreamingType;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.MuleConfiguration;
import org.mule.runtime.extension.api.notification.NotificationEmitter;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Feature;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpErrorMessageGenerator;

@Feature(HTTP_EXTENSION)
public class HttpRequestTracingContextTestCase {

  public static final String TRACE_CONTEXT_KEY_1 = "KEY1";
  public static final String TRACE_CONTEXT_VALUE_1 = "VALUE1";
  @Mock
  private HttpResponseToResult httpResponseToResult;

  @Mock
  private HttpErrorMessageGenerator httpErrorMessageGenerator;

  @Mock
  private HttpExtensionClient client;

  @Mock
  private HttpRequesterConfig config;

  @Mock
  private HttpRequestAuthentication authentication;

  @Mock
  private ResponseValidator responseValidator;

  @Mock
  private TransformationService transformationService;

  @Mock
  private RequestCreator requestCreator;

  @Mock
  private MuleContext muleContext;

  @Mock
  private Scheduler scheduler;

  @Mock
  private NotificationEmitter notificationEmitter;

  @Mock
  private StreamingHelper streamingHelper;

  @Mock
  private CompletionCallback<InputStream, HttpResponseAttributes> callback;

  @Mock
  private Map<String, List<String>> injectedHeaders;

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Test
  public void testTraceContext() {
    HttpRequestFactory httpRequestFactory = new HttpRequestFactory();
    HttpRequester httpRequester = new HttpRequester(httpRequestFactory, httpResponseToResult, httpErrorMessageGenerator);
    DistributedTraceContextManager distributedTraceContextManager = mock(DistributedTraceContextManager.class);
    MuleConfiguration configuration = mock(MuleConfiguration.class);
    when(muleContext.getConfiguration()).thenReturn(configuration);
    HttpRequestBuilder httpRequesterBuilder = HttpRequest.builder();
    when(requestCreator.createRequestBuilder(any(HttpRequesterConfig.class))).thenReturn(httpRequesterBuilder);
    when(config.getDefaultHeaders()).thenReturn(emptyList());
    when(requestCreator.getCorrelationData()).thenReturn(empty());
    when(distributedTraceContextManager.getRemoteTraceContextMap()).thenReturn(ImmutableMap.of(TRACE_CONTEXT_KEY_1,
                                                                                               TRACE_CONTEXT_VALUE_1));
    when(requestCreator.getBody()).thenReturn(new TypedValue("payload", STRING));
    Message message = mock(Message.class);
    when(message.getPayload()).thenReturn(new TypedValue("payload".getBytes(StandardCharsets.UTF_8), BYTE_ARRAY));
    when(transformationService.transform(any(), any())).thenReturn(message);
    CompletableFuture completableFuture = mock(CompletableFuture.class);
    when(client.send(any(), anyInt(), anyBoolean(), any(), any())).thenReturn(completableFuture);
    httpRequester.doRequest(client, config, "http://dummyUri", "GET",
                            HttpStreamingType.ALWAYS, HttpSendBodyMode.ALWAYS,
                            true, authentication,
                            100, responseValidator,
                            transformationService, requestCreator,
                            true, muleContext, scheduler, notificationEmitter,
                            streamingHelper, callback,
                            injectedHeaders,
                            distributedTraceContextManager);
    assertThat(httpRequesterBuilder.getHeaderValue(TRACE_CONTEXT_KEY_1).isPresent(), equalTo(TRUE));
    assertThat(httpRequesterBuilder.getHeaderValue(TRACE_CONTEXT_KEY_1).get(), equalTo(TRACE_CONTEXT_VALUE_1));
  }
}
