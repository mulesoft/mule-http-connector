/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

import org.mule.extension.http.api.BaseHttpRequestAttributes;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.policy.OperationPolicyParametersTransformer;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Implementation that does transformation from http:request operation parameters to {@link Message} and vice versa.
 *
 * @since 1.0
 */
public class HttpPolicyRequestParametersTransformer implements OperationPolicyParametersTransformer {

  @Override
  public boolean supports(ComponentIdentifier componentIdentifier) {
    return componentIdentifier.equals(buildFromStringRepresentation("http:request"));
  }

  @Override
  public Message fromParametersToMessage(Map<String, Object> parameters) {
    String path = (String) parameters.get("path");
    TypedValue<Object> body = (TypedValue<Object>) parameters.getOrDefault("body", TypedValue.of(null));

    return Message.builder().value(body.getValue())
        .attributesValue(new HttpPolicyRequestAttributes(getMap(parameters, "headers"),
                                                         getMap(parameters, "queryParams"),
                                                         getMap(parameters, "uriParams"),
                                                         path))
        .mediaType(body.getDataType().getMediaType())
        .build();
  }

  @Override
  public Map<String, Object> fromMessageToParameters(Message message) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

    if (message.getAttributes().getValue() instanceof BaseHttpRequestAttributes) {
      BaseHttpRequestAttributes requestAttributes = (BaseHttpRequestAttributes) message.getAttributes().getValue();

      putIfNotNull(builder, "headers", requestAttributes.getHeaders());
      putIfNotNull(builder, "queryParams", requestAttributes.getQueryParams());
      putIfNotNull(builder, "uriParams", requestAttributes.getUriParams());
      putIfNotNull(builder, "path", requestAttributes.getRequestPath());
    }

    putIfNotNull(builder, "body", message.getPayload());

    return builder.build();
  }

  private void putIfNotNull(ImmutableMap.Builder<String, Object> builder, String key, Object value) {
    if (value != null) {
      builder.put(key, value);
    }
  }

  private MultiMap<String, String> getMap(Map<String, Object> parameters, String key) {
    return (MultiMap<String, String>) parameters.getOrDefault(key, new MultiMap<String, String>());
  }
}
