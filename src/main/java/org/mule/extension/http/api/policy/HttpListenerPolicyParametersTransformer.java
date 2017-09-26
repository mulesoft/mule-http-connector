/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.listener.builder.HttpListenerErrorResponseBuilder;
import org.mule.extension.http.api.listener.builder.HttpListenerResponseBuilder;
import org.mule.extension.http.api.listener.builder.HttpListenerSuccessResponseBuilder;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.policy.SourcePolicyParametersTransformer;
import org.mule.runtime.api.util.MultiMap;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Implementation that does transformation from http:listener response and failure response parameters to {@link Message} and vice
 * versa.
 *
 * @since 1.0
 */
public class HttpListenerPolicyParametersTransformer implements SourcePolicyParametersTransformer {

  @Override
  public boolean supports(ComponentIdentifier componentIdentifier) {
    return componentIdentifier.equals(buildFromStringRepresentation("http:listener"));
  }

  @Override
  public Message fromSuccessResponseParametersToMessage(Map<String, Object> parameters) {
    HttpListenerResponseBuilder responseBuilder =
        (HttpListenerResponseBuilder) parameters.get(ResponseType.SUCCESS.getResponseBuilderParameterName());
    return responseParametersToMessage(responseBuilder, ResponseType.SUCCESS.getStatusCode());
  }

  private Message responseParametersToMessage(HttpListenerResponseBuilder responseBuilder, int defaultStatusCode) {
    MultiMap<String, String> headers = new MultiMap<>(responseBuilder.getHeaders());
    Message.Builder messageBuilder;
    Message.PayloadBuilder builder = Message.builder();
    TypedValue<Object> body = responseBuilder.getBody();
    if (body.getValue() == null) {
      messageBuilder = builder.nullValue();
    } else {
      messageBuilder = builder.value(body.getValue()).mediaType(body.getDataType().getMediaType());
    }
    int statusCode = responseBuilder.getStatusCode() == null ? defaultStatusCode : responseBuilder.getStatusCode();
    return messageBuilder
        .attributesValue(new HttpResponseAttributes(statusCode, responseBuilder.getReasonPhrase(), headers))
        .build();
  }

  @Override
  public Message fromFailureResponseParametersToMessage(Map<String, Object> parameters) {
    HttpListenerResponseBuilder responseBuilder =
        (HttpListenerResponseBuilder) parameters.get(ResponseType.FAILURE.getResponseBuilderParameterName());
    return responseParametersToMessage(responseBuilder, ResponseType.FAILURE.getStatusCode());
  }

  @Override
  public Map<String, Object> fromMessageToSuccessResponseParameters(Message message) {
    return messageToResponseParameters(new HttpListenerSuccessResponseBuilder(), message,
                                       ResponseType.SUCCESS);
  }

  @Override
  public Map<String, Object> fromMessageToErrorResponseParameters(Message message) {
    return messageToResponseParameters(new HttpListenerErrorResponseBuilder(), message,
                                       ResponseType.FAILURE);
  }

  private Map<String, Object> messageToResponseParameters(HttpListenerResponseBuilder httpListenerResponseBuilder,
                                                          Message message,
                                                          ResponseType responseType) {
    ImmutableMap.Builder<String, Object> mapBuilder =
        ImmutableMap.<String, Object>builder().put(responseType.getResponseBuilderParameterName(), httpListenerResponseBuilder);
    if (message.getAttributes().getValue() instanceof HttpResponseAttributes) {
      HttpResponseAttributes httpResponseAttributes = (HttpResponseAttributes) message.getAttributes().getValue();
      if (ResponseType.SUCCESS.equals(responseType)) {
        httpListenerResponseBuilder.setBody(message.getPayload());
        httpListenerResponseBuilder.setStatusCode(httpResponseAttributes.getStatusCode() == 0 ? responseType.getStatusCode()
            : httpResponseAttributes.getStatusCode());
      } else {
        httpListenerResponseBuilder.setStatusCode(responseType.getStatusCode());

      }
      httpListenerResponseBuilder.setHeaders(httpResponseAttributes.getHeaders());
      httpListenerResponseBuilder.setReasonPhrase(httpResponseAttributes.getReasonPhrase());
      return mapBuilder.build();
    } else if (message.getAttributes().getValue() instanceof HttpPolicyResponseAttributes) {
      HttpPolicyResponseAttributes httpResponseAttributes = (HttpPolicyResponseAttributes) message.getAttributes().getValue();
      if (ResponseType.SUCCESS.equals(responseType)) {
        httpListenerResponseBuilder.setBody(message.getPayload());
      }
      httpListenerResponseBuilder.setHeaders(httpResponseAttributes.getHeaders());
      httpListenerResponseBuilder.setStatusCode(httpResponseAttributes.getStatusCode() == 0 ? responseType.getStatusCode()
          : httpResponseAttributes.getStatusCode());
      httpListenerResponseBuilder.setReasonPhrase(httpResponseAttributes.getReasonPhrase());
      return mapBuilder.build();
    } else {
      if (ResponseType.SUCCESS.equals(responseType)) {
        httpListenerResponseBuilder.setBody(message.getPayload());
        httpListenerResponseBuilder
            .setStatusCode(httpListenerResponseBuilder.getStatusCode() == null ? responseType.getStatusCode()
                : httpListenerResponseBuilder.getStatusCode());
      } else {
        httpListenerResponseBuilder.setStatusCode(responseType.getStatusCode());
      }
      return mapBuilder.build();
    }
  }
}


enum ResponseType {
  SUCCESS(200, "response"), FAILURE(500, "errorResponse");

  private int statusCode;
  private String responseBuilderParameterName;

  ResponseType(int statusCode, String responseBuilderParameterName) {
    this.statusCode = statusCode;
    this.responseBuilderParameterName = responseBuilderParameterName;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getResponseBuilderParameterName() {
    return responseBuilderParameterName;
  }
}
