/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static org.mule.extension.http.internal.request.profiling.tracing.HttpSpanUtils.addStatusCodeAttribute;
import static org.mule.extension.http.internal.request.profiling.tracing.HttpSpanUtils.updateServerSpanStatus;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.INTERNAL_SERVER_ERROR;

import org.mule.extension.http.api.listener.builder.HttpListenerResponseBuilder;
import org.mule.extension.http.internal.listener.intercepting.Interception;
import org.mule.extension.http.internal.service.server.HttpResponseReadyCallbackProxy;
import org.mule.extension.http.internal.service.server.ResponseStatusCallbackProxy;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.extension.api.runtime.source.SourceCompletionCallback;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.http.api.domain.message.response.HttpResponseBuilder;
import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;

import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpListenerResponseSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpListenerResponseSender.class);

  private final HttpResponseFactory responseFactory;
  private final Scheduler scheduler;

  public HttpListenerResponseSender(HttpResponseFactory responseFactory, Scheduler responseSenderScheduler) {
    this.responseFactory = responseFactory;
    this.scheduler = responseSenderScheduler;
  }

  public void sendResponse(HttpResponseContext context,
                           HttpListenerResponseBuilder response,
                           final SourceCompletionCallback completionCallback,
                           DistributedTraceContextManager distributedTraceContextManager) {
    final HttpResponse httpResponse = buildResponse(response, context.getInterception(), context.isSupportStreaming());
    final HttpResponseReadyCallbackProxy responseCallback = context.getResponseCallback();
    addStatusCodeAttribute(distributedTraceContextManager, httpResponse.getStatusCode(), LOGGER);
    updateServerSpanStatus(distributedTraceContextManager, httpResponse.getStatusCode(), LOGGER);
    if (context.isDeferredResponse()) {
      try {
        scheduler.submit(() -> {
          try {
            internalSendResponse(completionCallback, responseCallback, httpResponse);
          } catch (Exception e) {
            completionCallback.error(e);
          }
        });
      } catch (RejectedExecutionException rejectedExecutionException) {
        internalSendResponse(completionCallback, responseCallback, httpResponse);
      }
    } else {
      internalSendResponse(completionCallback, responseCallback, httpResponse);
    }
  }

  private void internalSendResponse(SourceCompletionCallback completionCallback,
                                    HttpResponseReadyCallbackProxy responseCallback,
                                    HttpResponse httpResponse) {
    responseCallback.responseReady(httpResponse, getResponseFailureCallback(responseCallback, completionCallback));
  }

  protected HttpResponse buildResponse(HttpListenerResponseBuilder listenerResponseBuilder, Interception interception,
                                       boolean supportStreaming) {
    return responseFactory.create(HttpResponse.builder(), interception, listenerResponseBuilder, supportStreaming);
  }

  public ResponseStatusCallbackProxy getResponseFailureCallback(HttpResponseReadyCallbackProxy responseReadyCallback,
                                                                SourceCompletionCallback completionCallback) {
    return new FailureResponseStatusCallback(responseReadyCallback, completionCallback);
  }

  /**
   * Implemented as an inner class instead of an anonymous class so that no problem arises in case reflection is needed for
   * retrieval of methods. This may be the case for backward compatibility concerns.
   */
  public static class FailureResponseStatusCallback implements ResponseStatusCallbackProxy {

    private HttpResponseReadyCallbackProxy responseReadyCallback;
    private SourceCompletionCallback completionCallback;

    public FailureResponseStatusCallback(HttpResponseReadyCallbackProxy responseReadyCallback,
                                         SourceCompletionCallback completionCallback) {
      this.responseReadyCallback = responseReadyCallback;
      this.completionCallback = completionCallback;
    }

    @Override
    public void responseSendFailure(Throwable throwable) {
      try {
        responseReadyCallback.responseReady(buildErrorResponse(), this);
        completionCallback.success();
      } catch (Throwable t) {
        completionCallback.error(t);
      }
    }

    @Override
    public void responseSendSuccessfully() {
      // TODO: MULE-9749 Figure out how to handle this. Maybe doing nothing is right since this will be executed later if
      // everything goes right.
      // responseCompletationCallback.responseSentSuccessfully();
      completionCallback.success();
    }

    public void onErrorSendingResponse(Throwable throwable) {
      completionCallback.error(throwable);
    }

    protected HttpResponse buildErrorResponse() {
      final HttpResponseBuilder errorResponseBuilder = HttpResponse.builder();
      final HttpResponse errorResponse = errorResponseBuilder.statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
          .reasonPhrase(INTERNAL_SERVER_ERROR.getReasonPhrase())
          .build();
      return errorResponse;
    }
  }

}
