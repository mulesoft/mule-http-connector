/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static java.util.Collections.singletonMap;
import static org.mule.extension.http.api.policy.HttpListenerPolicyParametersTransformer.ResponseType.FAILURE;
import static org.mule.extension.http.api.policy.HttpListenerPolicyParametersTransformer.ResponseType.SUCCESS;
import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.listener.builder.HttpListenerErrorResponseBuilder;
import org.mule.extension.http.api.listener.builder.HttpListenerResponseBuilder;
import org.mule.extension.http.api.listener.builder.HttpListenerSuccessResponseBuilder;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.policy.SourcePolicyParametersTransformer;

import java.util.Map;

/**
 * Implementation that does transformation from http:listener response and failure response parameters to {@link Message} and vice
 * versa.
 *
 * @since 1.0
 */
public class HttpListenerPolicyParametersTransformer implements SourcePolicyParametersTransformer {

  private static final DataType HTTP_RESPONSE_ATTRIBUTES_DATATYPE = DataType.fromType(HttpResponseAttributes.class);

  @Override
  public boolean supports(ComponentIdentifier componentIdentifier) {
    return componentIdentifier.equals(buildFromStringRepresentation("http:listener"));
  }

  @Override
  public Message fromSuccessResponseParametersToMessage(Map<String, Object> parameters) {
    HttpListenerResponseBuilder responseBuilder =
        (HttpListenerResponseBuilder) parameters.get(SUCCESS.getResponseBuilderParameterName());
    return responseParametersToMessage(responseBuilder, SUCCESS.getStatusCode());
  }

  @Override
  public Message fromFailureResponseParametersToMessage(Map<String, Object> parameters) {
    HttpListenerResponseBuilder responseBuilder =
        (HttpListenerResponseBuilder) parameters.get(FAILURE.getResponseBuilderParameterName());
    return responseParametersToMessage(responseBuilder, FAILURE.getStatusCode());
  }

  private Message responseParametersToMessage(HttpListenerResponseBuilder responseBuilder, int defaultStatusCode) {
    int statusCode = responseBuilder.getStatusCode() == null ? defaultStatusCode : responseBuilder.getStatusCode();
    return Message.builder()
        .payload(responseBuilder.getBody())
        .attributes(new TypedValue<>(new HttpResponseAttributes(statusCode, responseBuilder.getReasonPhrase(),
                                                                responseBuilder.getHeaders()),
                                     HTTP_RESPONSE_ATTRIBUTES_DATATYPE))
        .build();
  }

  @Override
  public Map<String, Object> fromMessageToSuccessResponseParameters(Message message) {
    return messageToResponseParameters(new HttpListenerSuccessResponseBuilder(), message,
                                       SUCCESS);
  }

  @Override
  public Map<String, Object> fromMessageToErrorResponseParameters(Message message) {
    return messageToResponseParameters(new HttpListenerErrorResponseBuilder(), message,
                                       FAILURE);
  }

  private Map<String, Object> messageToResponseParameters(HttpListenerResponseBuilder httpListenerResponseBuilder,
                                                          Message message,
                                                          ResponseType responseType) {
    if (message.getAttributes().getValue() instanceof HttpResponseAttributes) {
      HttpResponseAttributes httpResponseAttributes = (HttpResponseAttributes) message.getAttributes().getValue();
      httpListenerResponseBuilder.setBody(message.getPayload());
      httpListenerResponseBuilder.setStatusCode(
                                                httpResponseAttributes.getStatusCode() == 0 ? responseType.getStatusCode()
                                                    : httpResponseAttributes.getStatusCode());
      httpListenerResponseBuilder.setHeaders(httpResponseAttributes.getHeaders());
      httpListenerResponseBuilder.setReasonPhrase(httpResponseAttributes.getReasonPhrase());
    } else if (message.getAttributes().getValue() instanceof HttpPolicyResponseAttributes) {
      HttpPolicyResponseAttributes httpResponseAttributes = (HttpPolicyResponseAttributes) message.getAttributes().getValue();
      httpListenerResponseBuilder.setBody(message.getPayload());
      httpListenerResponseBuilder.setHeaders(httpResponseAttributes.getHeaders());
      httpListenerResponseBuilder.setStatusCode(httpResponseAttributes.getStatusCode() == null ? responseType.getStatusCode()
          : httpResponseAttributes.getStatusCode());
      httpListenerResponseBuilder.setReasonPhrase(httpResponseAttributes.getReasonPhrase());
    } else {
      httpListenerResponseBuilder.setBody(message.getPayload());
      httpListenerResponseBuilder.setStatusCode(httpListenerResponseBuilder.getStatusCode() == null ? responseType.getStatusCode()
          : httpListenerResponseBuilder.getStatusCode());

    }

    return singletonMap(responseType.getResponseBuilderParameterName(), httpListenerResponseBuilder);
  }

  enum ResponseType {
    SUCCESS(200, "response"), FAILURE(500, "errorResponse");

    private final Integer statusCode;
    private final String responseBuilderParameterName;

    ResponseType(Integer statusCode, String responseBuilderParameterName) {
      this.statusCode = statusCode;
      this.responseBuilderParameterName = responseBuilderParameterName;
    }

    public Integer getStatusCode() {
      return statusCode;
    }

    public String getResponseBuilderParameterName() {
      return responseBuilderParameterName;
    }
  }
}


