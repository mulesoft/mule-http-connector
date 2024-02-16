/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.api.request.HttpSendBodyMode.ALWAYS;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Caches methods to avoid repeating reflection calls.
 */
public final class HttpClientReflection {

  public static final String HTTP_REQUEST_OPTIONS_CLASS_NAME =
      "org.mule.runtime.http.api.client.HttpRequestOptions";

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
      Class<?> httpRequestOptionsClass = Class.forName(HTTP_REQUEST_OPTIONS_CLASS_NAME);
      sendAsyncMethod = Class.forName("org.mule.runtime.http.api.client.HttpClient")
          .getDeclaredMethod("sendAsync", HttpRequest.class, httpRequestOptionsClass);

      builderMethod = httpRequestOptionsClass.getDeclaredMethod("builder");
      buildMethod = httpRequestOptionsClass.getDeclaredMethod("build");
      responseTimeoutMethod = httpRequestOptionsClass.getDeclaredMethod("responseTimeout", int.class);
      followsRedirectMethod = httpRequestOptionsClass.getDeclaredMethod("followsRedirect", boolean.class);
      authenticationMethod = httpRequestOptionsClass.getDeclaredMethod("authentication", HttpAuthentication.class);
      sendBodyAlwaysMethod = httpRequestOptionsClass.getDeclaredMethod("sendBodyAlways", boolean.class);
    } catch (Exception ignored) {
      loaded = false;
    }
  }

  private static Object requestOptions(int responseTimeout, boolean followsRedirect,
                                       HttpAuthentication authentication, HttpSendBodyMode sendBodyMode)
      throws Exception {
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
        return (CompletableFuture<HttpResponse>) sendAsyncMethod
            .invoke(client, request, requestOptions(responseTimeout, followRedirects, authentication, sendBodyMode));
      } catch (Exception ignored) {
      }
    }

    return client.sendAsync(request, responseTimeout, followRedirects, authentication);
  }
}
