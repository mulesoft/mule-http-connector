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
import org.mule.runtime.http.policy.api.HttpRequestMessage;
import org.mule.runtime.http.policy.api.HttpResponseMessage;
import org.mule.runtime.http.policy.api.PolicyIsolationTransformer;

import java.net.URI;

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
      HttpRequestMessage anchor =
          new HttpRequestMessage(attributes.getHeaders(), attributes.getScheme(),
                                 attributes.getLocalAddress(), 8080,
                                 attributes.getRawRequestPath(), attributes.getMethod());
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

      URI uri = URI.create(requestMessage.getRawPath());

      HttpRequestAttributes requestAttributes =
          HttpRequestAttributes.builder()
              .headers(requestMessage.getHeaders())
              .listenerPath("/*")
              .localAddress(requestMessage.getDomain())
              .method(requestMessage.getMethod())
              .queryParams(new MultiMap<>())
              .queryString("")
              .rawRequestPath(requestMessage.getRawPath())
              .requestPath(uri.getPath())
              .remoteAddress("127.0.0.1")
              .relativePath(uri.getPath())
              .requestUri(uri.getPath())
              .scheme(requestMessage.getScheme())
              .uriParams(new MultiMap<>())
              .version("1.1")
              .build();

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
