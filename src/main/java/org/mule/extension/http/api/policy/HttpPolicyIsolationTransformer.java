/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.http.policy.api.PolicyIsolationTransformer;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpPolicyIsolationTransformer implements PolicyIsolationTransformer, Initialisable {

  protected static final Logger LOGGER = LoggerFactory.getLogger(HttpPolicyIsolationTransformer.class);

  @Inject
  private MuleContext muleContext;

  @Override
  public void initialise() {
    LOGGER.warn("Initialising {} for {}", HttpPolicyIsolationTransformer.class.getName(), muleContext.getConfiguration().getId());
  }

  @Override
  public Message isolate(Message message) {

    Message message1 = message;

    if (message.getAttributes().getValue() instanceof HttpRequestAttributes) {
      HttpRequestAttributes attributes = (HttpRequestAttributes) message.getAttributes().getValue();

      Map<String, Object> attributesMap = new HashMap<>(18);
      attributesMap.put("type", attributes.getClass().getName());
      attributesMap.put("listenerPath", attributes.getListenerPath());
      attributesMap.put("localAddress", attributes.getLocalAddress());
      attributesMap.put("maskedRequestPath", attributes.getMaskedRequestPath());
      attributesMap.put("method", attributes.getMethod());
      attributesMap.put("queryString", attributes.getQueryString());
      attributesMap.put("rawRequestPath", attributes.getRawRequestPath());
      attributesMap.put("rawRequestUri", attributes.getRawRequestUri());
      attributesMap.put("relativePath", attributes.getRelativePath());
      attributesMap.put("remoteAddress", attributes.getRemoteAddress());
      attributesMap.put("requestUri", attributes.getRequestUri());
      attributesMap.put("scheme", attributes.getScheme());
      attributesMap.put("version", attributes.getVersion());
      attributesMap.put("headers", attributes.getHeaders());
      attributesMap.put("queryParams", attributes.getQueryParams());
      attributesMap.put("requestPath", attributes.getRequestPath());
      attributesMap.put("uriParams", attributes.getUriParams());
      message1 = Message.builder(message).attributesValue(attributesMap).build();

      // HttpRequestMessage anchor =
      // new HttpRequestMessage(attributes.getHeaders(), attributes.getScheme(),
      // attributes.getLocalAddress(), 8080,
      // attributes.getRawRequestPath(), attributes.getMethod());
      // message1 = Message.builder(message).attributesValue(anchor).build();
    } else if (message.getAttributes().getValue() instanceof HttpResponseAttributes) {
      HttpResponseAttributes attributes = (HttpResponseAttributes) message.getAttributes().getValue();

      Map<String, Object> attributesMap = new HashMap<>(4);
      attributesMap.put("type", attributes.getClass().getName());
      attributesMap.put("reasonPhrase", attributes.getReasonPhrase());
      attributesMap.put("statusCode", attributes.getStatusCode());
      attributesMap.put("headers", attributes.getHeaders());
      message1 = Message.builder(message).attributesValue(attributesMap).build();

      // HttpResponseMessage anchor =
      // new HttpResponseMessage(attributes.getHeaders(), attributes.getStatusCode());
      // message1 = Message.builder(message).attributesValue(anchor).build();
    }

    return message1;
  }

  @Override
  public Message desolate(Message message) {

    Message message1 = message;

    if (message.getAttributes().getValue() instanceof Map) {
      Map<String, Object> attributesMap = (Map<String, Object>) message.getAttributes().getValue();

      if (HttpRequestAttributes.class.getName().equals(attributesMap.get("type"))) {

        HttpRequestAttributes requestAttributes =
            HttpRequestAttributes.builder()
                .headers((MultiMap<String, String>) attributesMap.get("headers"))
                .listenerPath((String) attributesMap.get("listenerPath"))
                .localAddress((String) attributesMap.get("localAddress"))
                .method((String) attributesMap.get("method"))
                .queryParams((MultiMap<String, String>) attributesMap.get("queryParams"))
                .queryString((String) attributesMap.get("queryString"))
                .rawRequestPath((String) attributesMap.get("rawRequestPath"))
                .rawRequestUri((String) attributesMap.get("rawRequestUri"))
                .requestPath((String) attributesMap.get("requestPath"))
                .remoteAddress((String) attributesMap.get("remoteAddress"))
                .relativePath((String) attributesMap.get("relativePath"))
                .requestUri((String) attributesMap.get("requestUri"))
                .scheme((String) attributesMap.get("scheme"))
                .uriParams((Map<String, String>) attributesMap.get("uriParams"))
                .version((String) attributesMap.get("version"))
                .build();

        message1 = Message.builder(message).attributesValue(requestAttributes).build();

      } else if (HttpResponseAttributes.class.getName().equals(attributesMap.get("type"))) {

        HttpResponseAttributes responseAttributes =
            new HttpResponseAttributes((int) attributesMap.get("statusCode"),
                                       (String) attributesMap.get("reasonPhrase"),
                                       (MultiMap<String, String>) attributesMap.get("headers"));
        message1 = Message.builder(message).attributesValue(responseAttributes).build();

      }
    }

    // if (message.getAttributes().getValue() instanceof HttpRequestMessage) {
    // HttpRequestMessage requestMessage = (HttpRequestMessage) message.getAttributes().getValue();
    //
    // URI uri = URI.create(requestMessage.getRawPath());
    //
    // HttpRequestAttributes requestAttributes =
    // HttpRequestAttributes.builder()
    // .headers(requestMessage.getHeaders())
    // .listenerPath("/*")
    // .localAddress(requestMessage.getDomain())
    // .method(requestMessage.getMethod())
    // .queryParams(new MultiMap<>())
    // .queryString("")
    // .rawRequestPath(requestMessage.getRawPath())
    // .requestPath(uri.getPath())
    // .remoteAddress("127.0.0.1")
    // .relativePath(uri.getPath())
    // .requestUri(uri.getPath())
    // .scheme(requestMessage.getScheme())
    // .uriParams(new MultiMap<>())
    // .version("1.1")
    // .build();
    //
    // message1 = Message.builder(message).attributesValue(requestAttributes).build();
    // } else if (message.getAttributes().getValue() instanceof HttpResponseMessage) {
    // HttpResponseMessage responseMessage = (HttpResponseMessage) message.getAttributes().getValue();
    // HttpResponseAttributes responseAttributes =
    // new HttpResponseAttributes(responseMessage.getStatusCode(), null, responseMessage.getHeaders());
    // message1 = Message.builder(message).attributesValue(responseAttributes).build();
    // }

    return message1;
  }
}
