/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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

public class HttpClientReflection {

  public static final String HTTP_REQUEST_OPTIONS_CLASS_NAME =
      "org.mule.runtime.http.api.client.HttpRequestOptions";

  private Object requestOptionsBuilder;
  Method sendAsyncMethod;
  Method buildMethod;
  Method responseTimeoutMethod;
  Method followsRedirectMethod;
  Method authenticationMethod;
  Method sendBodyAlwaysMethod;

  boolean loaded = true;

  public HttpClientReflection(HttpClient client) {
    try {
      Class<?> httpRequestOptionsClass = Class.forName(HTTP_REQUEST_OPTIONS_CLASS_NAME);
      sendAsyncMethod = client.getClass()
          .getDeclaredMethod("sendAsync", HttpRequest.class, httpRequestOptionsClass);

      Method builderMethod = httpRequestOptionsClass.getDeclaredMethod("builder");
      requestOptionsBuilder = builderMethod.invoke(null);
      buildMethod = requestOptionsBuilder.getClass().getDeclaredMethod("build");
      responseTimeoutMethod = requestOptionsBuilder.getClass().getDeclaredMethod("responseTimeout", int.class);
      followsRedirectMethod = requestOptionsBuilder.getClass().getDeclaredMethod("followsRedirect", boolean.class);
      authenticationMethod = requestOptionsBuilder.getClass().getDeclaredMethod("authentication", HttpAuthentication.class);
      sendBodyAlwaysMethod = requestOptionsBuilder.getClass().getDeclaredMethod("sendBodyAlways", boolean.class);
    } catch (Exception ignored) {
      loaded = false;
    }
  }

  Object requestOptions(int responseTimeout, boolean followsRedirect,
                        HttpAuthentication authentication, HttpSendBodyMode sendBodyMode)
      throws Exception {
    requestOptionsBuilder = responseTimeoutMethod.invoke(requestOptionsBuilder, responseTimeout);
    requestOptionsBuilder = followsRedirectMethod.invoke(requestOptionsBuilder, followsRedirect);
    requestOptionsBuilder = authenticationMethod.invoke(requestOptionsBuilder, authentication);
    requestOptionsBuilder = sendBodyAlwaysMethod.invoke(requestOptionsBuilder, sendBodyMode.equals(ALWAYS));

    return buildMethod.invoke(requestOptionsBuilder);
  }

  public CompletableFuture<HttpResponse> sendAsync(HttpClient client, HttpRequest request, int responseTimeout,
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
