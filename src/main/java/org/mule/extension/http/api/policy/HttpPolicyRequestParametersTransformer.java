/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;
import static org.mule.runtime.api.metadata.DataType.OBJECT;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap.emptyCaseInsensitiveMultiMap;

import org.mule.extension.http.api.BaseHttpRequestAttributes;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.policy.OperationPolicyParametersTransformer;

import com.google.common.collect.ImmutableMap;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;

import java.util.Map;

/**
 * Implementation that does transformation from http:request operation parameters to {@link Message} and vice versa.
 *
 * @since 1.0
 */
public class HttpPolicyRequestParametersTransformer implements OperationPolicyParametersTransformer {

  private static final DataType HTTP_POLICY_REQUEST_ATTRIBUTES_DATATYPE = DataType.fromType(HttpPolicyRequestAttributes.class);

  private static final String BODY = "body";
  private static final String PATH = "path";
  private static final String HEADERS = "headers";
  private static final String QUERY_PARAMS = "queryParams";
  private static final String URI_PARAMS = "uriParams";

  @Override
  public boolean supports(ComponentIdentifier componentIdentifier) {
    return componentIdentifier.equals(buildFromStringRepresentation("http:request"));
  }

  @Override
  public Message fromParametersToMessage(Map<String, Object> parameters) {
    TypedValue<Object> body = (TypedValue<Object>) parameters.getOrDefault(BODY, new TypedValue<Object>(null, OBJECT));

    return Message.builder().payload(body)
        .attributes(new TypedValue<>(new HttpPolicyRequestAttributes(getCaseInsensitiveMultiMap(parameters, HEADERS),
                                                                     getMultiMap(parameters, QUERY_PARAMS),
                                                                     getMap(parameters, URI_PARAMS),
                                                                     (String) parameters.get(PATH)),
                                     HTTP_POLICY_REQUEST_ATTRIBUTES_DATATYPE))
        .build();
  }

  @Override
  public Map<String, Object> fromMessageToParameters(Message message) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

    if (message.getAttributes().getValue() instanceof BaseHttpRequestAttributes) {
      BaseHttpRequestAttributes requestAttributes = (BaseHttpRequestAttributes) message.getAttributes().getValue();

      putIfNotNull(builder, PATH, requestAttributes.getRequestPath());
      putIfNotNull(builder, HEADERS, requestAttributes.getHeaders());
      putIfNotNull(builder, QUERY_PARAMS, requestAttributes.getQueryParams());
      putIfNotNull(builder, URI_PARAMS, requestAttributes.getUriParams());
    }

    putIfNotNull(builder, "body", message.getPayload());

    return builder.build();
  }

  private void putIfNotNull(ImmutableMap.Builder<String, Object> builder, String key, Object value) {
    if (value != null) {
      builder.put(key, value);
    }
  }

  private MultiMap<String, String> getMultiMap(Map<String, Object> parameters, String key) {
    return (MultiMap<String, String>) parameters.getOrDefault(key, emptyMultiMap());
  }

  private Map<String, String> getMap(Map<String, Object> parameters, String key) {
    return (Map<String, String>) parameters.getOrDefault(key, emptyMultiMap());
  }

  private CaseInsensitiveMultiMap getCaseInsensitiveMultiMap(Map<String, Object> parameters, String key) {
    return (CaseInsensitiveMultiMap) parameters.getOrDefault(key, emptyCaseInsensitiveMultiMap());
  }
}
