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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HttpClientReflection {

  final ConcurrentMap<String, Method> cache = new ConcurrentHashMap<>();

  Method getSendAsync(HttpClient delegate) throws NoSuchMethodException, ClassNotFoundException {
    if (cache.containsKey("sendAsync")) {
      return cache.get("sendAsync");
    }

    Method method = delegate.getClass()
        .getDeclaredMethod("sendAsync", HttpRequest.class,
                           Class.forName("org.mule.runtime.http.api.client.HttpRequestOptions"));
    return cache.computeIfAbsent("sendAsync", k -> method);
  }

  Object requestOptions(int responseTimeout, boolean followsRedirect,
                        HttpAuthentication authentication, HttpSendBodyMode sendBodyMode)
      throws Exception {
    Object requestOptionsBuilder = getBuilder().invoke(null);
    requestOptionsBuilder = with("responseTimeout", responseTimeout, requestOptionsBuilder, int.class);
    requestOptionsBuilder = with("followsRedirect", followsRedirect, requestOptionsBuilder, boolean.class);
    requestOptionsBuilder = with("authentication", authentication, requestOptionsBuilder, HttpAuthentication.class);
    requestOptionsBuilder =
        with("sendBody", sendBodyMode.equals(ALWAYS), requestOptionsBuilder, boolean.class);

    return getBuild(requestOptionsBuilder).invoke(requestOptionsBuilder);
  }

  private Method getBuild(Object requestOptionsBuilder) throws NoSuchMethodException {
    if (cache.containsKey("build")) {
      return cache.get("build");
    }

    Method method = requestOptionsBuilder.getClass().getDeclaredMethod("build");
    return cache.computeIfAbsent("build", k -> method);
  }

  private Method getBuilder() throws NoSuchMethodException, ClassNotFoundException {
    if (cache.containsKey("builder")) {
      return cache.get("builder");
    }

    Method method = Class.forName("org.mule.runtime.http.api.client.HttpRequestOptions").getDeclaredMethod("builder");
    return cache.computeIfAbsent("builder", k -> method);
  }

  private Object with(String name, Object arg, Object requestOptionsBuilder, Class<?> clazz)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Method method;
    if (cache.containsKey(name)) {
      method = cache.get(name);
    } else {
      method = requestOptionsBuilder.getClass().getDeclaredMethod(name, clazz);
      cache.putIfAbsent(name, method);
    }
    return method.invoke(requestOptionsBuilder, arg);
  }

}
