/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.api.request.HttpSendBodyMode.ALWAYS;

import static java.lang.Class.forName;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Caches methods to avoid repeating reflection calls.
 */
public final class HttpClientReflection {

  public static final String HTTP_CLIENT_CLASS_NAME =
      "org.mule.runtime.http.api.client.HttpClient";

  public static final String HTTP_REQUEST_OPTIONS_CLASS_NAME =
      "org.mule.runtime.http.api.client.HttpRequestOptions";

  public static final String HTTP_REQUEST_OPTIONS_BUILDER_CLASS_NAME =
      "org.mule.runtime.http.api.client.HttpRequestOptionsBuilder";

  private HttpClientReflection() {
    // Empty private constructor to avoid instantiation.
  }

  private static Method builderMethod;
  private static Method sendAsyncMethod;
  private static Method buildMethod;
  private static Method responseTimeoutMethod;
  private static Method followsRedirectMethod;
  private static Method authenticationMethod;
  private static Method sendBodyAlwaysMethod;

  static boolean loaded = true;

  static {
    try {
      Class<?> httpRequestOptionsClass = forName(HTTP_REQUEST_OPTIONS_CLASS_NAME);
      builderMethod = httpRequestOptionsClass.getDeclaredMethod("builder");

      Class<?> builderClass = forName(HTTP_REQUEST_OPTIONS_BUILDER_CLASS_NAME);
      buildMethod = builderClass.getDeclaredMethod("build");
      responseTimeoutMethod = builderClass.getDeclaredMethod("responseTimeout", int.class);
      followsRedirectMethod = builderClass.getDeclaredMethod("followsRedirect", boolean.class);
      authenticationMethod = builderClass.getDeclaredMethod("authentication", HttpAuthentication.class);
      sendBodyAlwaysMethod = builderClass.getDeclaredMethod("sendBodyAlways", boolean.class);

      Class<?> httpClientClass = forName(HTTP_CLIENT_CLASS_NAME);
      sendAsyncMethod = httpClientClass.getDeclaredMethod("sendAsync", HttpRequest.class, httpRequestOptionsClass);
    } catch (Exception ignored) {
      loaded = false;
    }
  }

  private static Object requestOptions(int responseTimeout, boolean followsRedirect,
                                       HttpAuthentication authentication, HttpSendBodyMode sendBodyMode)
      throws InvocationTargetException, IllegalAccessException {
    Object requestOptionsBuilder = builderMethod.invoke(null);
    requestOptionsBuilder = responseTimeoutMethod.invoke(requestOptionsBuilder, responseTimeout);
    requestOptionsBuilder = followsRedirectMethod.invoke(requestOptionsBuilder, followsRedirect);
    requestOptionsBuilder = authenticationMethod.invoke(requestOptionsBuilder, authentication);
    requestOptionsBuilder = sendBodyAlwaysMethod.invoke(requestOptionsBuilder, sendBodyMode.equals(ALWAYS));
    return buildMethod.invoke(requestOptionsBuilder);
  }

  public static CompletableFuture<HttpResponse> sendAsync(HttpClient client, HttpRequest request, int responseTimeout,
                                                          boolean followRedirects, HttpAuthentication authentication,
                                                          HttpSendBodyMode sendBodyMode) {
    if (loaded) {
      try {
        return invokeSendAsyncUnsafe(client, request, responseTimeout, followRedirects, authentication, sendBodyMode);
      } catch (InvocationTargetException e) {
        throw wrapRuntimeException(e.getTargetException());
      } catch (IllegalAccessException e) {
        return client.sendAsync(request, responseTimeout, followRedirects, authentication);
      }
    } else {
      return client.sendAsync(request, responseTimeout, followRedirects, authentication);
    }
  }

  private static CompletableFuture<HttpResponse> invokeSendAsyncUnsafe(HttpClient client, HttpRequest request,
                                                                       int responseTimeout, boolean followRedirects,
                                                                       HttpAuthentication authentication,
                                                                       HttpSendBodyMode sendBodyMode)
      throws IllegalAccessException, InvocationTargetException {
    return (CompletableFuture<HttpResponse>) sendAsyncMethod
        .invoke(client, request, requestOptions(responseTimeout, followRedirects, authentication, sendBodyMode));
  }

  private static RuntimeException wrapRuntimeException(Throwable exception) {
    if (exception instanceof RuntimeException) {
      return (RuntimeException) exception;
    } else {
      return new RuntimeException(exception);
    }
  }
}
