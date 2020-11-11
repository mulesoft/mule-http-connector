/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.PolicyIsolationTransformer;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.policy.api.HttpRequestMessage;
import org.mule.runtime.http.policy.api.HttpResponseMessage;

import java.net.URI;
import java.net.URISyntaxException;

public class HttpPolicyIsolationTransformer implements PolicyIsolationTransformer {

  @Override
  public Message isolate(Message message) {

    Message message1 = message;

    if (message.getAttributes().getValue() instanceof HttpRequestAttributes) {
      HttpRequestAttributes attributes = (HttpRequestAttributes) message.getAttributes().getValue();
      HttpRequestMessage anchor =
          new HttpRequestMessage(attributes.getHeaders(), attributes.getRawRequestUri(), attributes.getMethod());
      message1 = Message.builder(message).attributesValue(anchor).build();
    } else if (message.getAttributes().getValue() instanceof HttpResponseAttributes) {
      HttpResponseAttributes attributes = (HttpResponseAttributes) message.getAttributes().getValue();
      HttpResponseMessage anchor =
          new HttpResponseMessage(attributes.getHeaders(), attributes.getStatusCode());
      message1 = Message.builder(message).attributesValue(anchor).build();
    }

    return message1;
  }

  @Override
  public Message desolate(Message message) {

    Message message1 = message;

    if (message.getAttributes().getValue() instanceof HttpRequestMessage) {
      HttpRequestMessage requestMessage = (HttpRequestMessage) message.getAttributes().getValue();
      URI uri;
      try {
        uri = new URI(requestMessage.getUrl());
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
      HttpRequestAttributes requestAttributes =
          new HttpRequestAttributes(requestMessage.getHeaders(), uri.getPath(), uri.getPath(), "1.1", uri.getScheme(),
                                    requestMessage.getMethod(), uri.getPath(), uri.getPath(), uri.getQuery(), new MultiMap<>(),
                                    new MultiMap<>(), "127.0.0.1", null);
      message1 = Message.builder(message).attributesValue(requestAttributes).build();
    } else if (message.getAttributes().getValue() instanceof HttpResponseMessage) {
      HttpResponseMessage responseMessage = (HttpResponseMessage) message.getAttributes().getValue();
      HttpResponseAttributes responseAttributes =
          new HttpResponseAttributes(responseMessage.getStatusCode(), null, responseMessage.getHeaders());
      message1 = Message.builder(message).attributesValue(responseAttributes).build();
    }

    return message1;
  }
}
