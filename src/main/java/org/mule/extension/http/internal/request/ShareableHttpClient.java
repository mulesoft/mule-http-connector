/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.api.request.HttpSendBodyMode.ALWAYS;
import static org.mule.extension.http.api.request.HttpSendBodyMode.NEVER;
import static org.mule.extension.http.internal.request.HttpRequestFactory.DEFAULT_EMPTY_BODY_METHODS;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Wrapper implementation of an {@link HttpClient} that allows being shared by only configuring the client when first required
 * and only disabling it when last required.
 */
public class ShareableHttpClient {

  private HttpClient delegate;
  private Integer usageCount = new Integer(0);

  public ShareableHttpClient(HttpClient client) {
    delegate = client;
  }

  public synchronized void start() {
    if (++usageCount == 1) {
      try {
        delegate.start();
      } catch (Exception e) {
        usageCount--;
        throw e;
      }
    }
  }

  public synchronized void stop() {
    // In case this fails we do not want the usageCount to be reincremented
    // as it will not be further used. If shouldn't be the case that more than
    // two stops happen.
    if (--usageCount == 0) {
      delegate.stop();
    }
  }

  public CompletableFuture<HttpResponse> sendAsync(HttpRequest request, int responseTimeout, boolean followRedirects,
                                                   HttpAuthentication authentication,
                                                   HttpSendBodyMode sendBodyMode) {
    try {
      Method sendAsync = delegate.getClass()
          .getDeclaredMethod("sendAsync", HttpRequest.class,
                             Class.forName("org.mule.runtime.http.api.client.HttpRequestOptions"));
      return (CompletableFuture<HttpResponse>) sendAsync
          .invoke(delegate, request, requestOptions(request, responseTimeout, followRedirects, authentication, sendBodyMode));
    } catch (Exception ignored) {
      return delegate.sendAsync(request, responseTimeout, followRedirects, authentication);
    }
  }

  private Object requestOptions(HttpRequest request, int responseTimeout, boolean followsRedirect,
                                HttpAuthentication authentication,
                                HttpSendBodyMode sendBodyMode)
      throws Exception {
    Object requestOptionsBuilder =
        Class.forName("org.mule.runtime.http.api.client.HttpRequestOptions").getDeclaredMethod("builder").invoke(null);
    requestOptionsBuilder = with("responseTimeout", responseTimeout, requestOptionsBuilder, int.class);
    requestOptionsBuilder = with("followsRedirect", followsRedirect, requestOptionsBuilder, boolean.class);
    requestOptionsBuilder = with("authentication", authentication, requestOptionsBuilder, HttpAuthentication.class);
    requestOptionsBuilder =
        with("sendBody", sendBodyMode.equals(ALWAYS), requestOptionsBuilder, boolean.class);

    return requestOptionsBuilder.getClass().getDeclaredMethod("build").invoke(requestOptionsBuilder);
  }

  private Object with(String name, Object arg, Object requestOptionsBuilder, Class<?> clazz)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    return requestOptionsBuilder.getClass().getDeclaredMethod(name, clazz).invoke(requestOptionsBuilder, arg);
  }
}
