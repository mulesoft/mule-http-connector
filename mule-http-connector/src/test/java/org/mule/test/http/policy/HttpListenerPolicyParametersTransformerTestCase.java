/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mule.runtime.api.metadata.DataType.ATOM_STRING;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.POLICY_SUPPORT;

import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.HttpResponseAttributesBuilder;
import org.mule.extension.http.api.listener.builder.HttpListenerErrorResponseBuilder;
import org.mule.extension.http.api.listener.builder.HttpListenerResponseBuilder;
import org.mule.extension.http.api.policy.HttpListenerPolicyParametersTransformer;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import com.google.common.collect.ImmutableMap;

import java.security.cert.Certificate;
import java.util.Map;
import java.util.OptionalLong;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
@Story(POLICY_SUPPORT)
public class HttpListenerPolicyParametersTransformerTestCase extends AbstractMuleTestCase {

  private static final String EXPECTED_PAYLOAD = "{'message': 'this is the payload'}";
  private static final MediaType EXPECTED_MEDIA_TYPE = MediaType.APPLICATION_JSON;

  //HttpRequestAttributes parameters
  private static final MultiMap<String, String> HEADERS = new MultiMap<>();
  private static final String LISTENER_PATH = "/test";
  private static final String RELATIVE_PATH = "/";
  private static final String VERSION = "1";
  private static final String SCHEME = "scheme";
  private static final String METHOD = "GET";
  private static final String REQUEST_PATH = "/test";
  private static final String REQUEST_URI = "";
  private static final String QUERY_STRING = "";
  private static final MultiMap<String, String> QUERY_PARAMS = new MultiMap<>();
  private static final MultiMap<String, String> URI_PARAMS = new MultiMap<>();
  private static final String REMOTE_ADDRESS = "";
  private static final String LOCAL_ADDRESS = "";
  private static final Certificate CLIENT_CERTIFICATE = null;

  private static final String REASON_PHRASE = "reasonPhrase";
  private static final Integer STATUS_CODE = 100;

  private HttpListenerPolicyParametersTransformer transformer = new HttpListenerPolicyParametersTransformer();

  @Test
  public void getErrorResponseBodyWhenSendingHttpRequestAttributes() {
    Message message = Message.builder().value(EXPECTED_PAYLOAD)
        .mediaType(EXPECTED_MEDIA_TYPE)
        .attributesValue(new HttpRequestAttributesBuilder()
            .headers(HEADERS)
            .listenerPath(LISTENER_PATH)
            .relativePath(RELATIVE_PATH)
            .version(VERSION)
            .scheme(SCHEME)
            .method(METHOD)
            .requestPath(REQUEST_PATH)
            .requestUri(REQUEST_URI)
            .queryString(QUERY_STRING)
            .queryParams(QUERY_PARAMS)
            .uriParams(URI_PARAMS)
            .localAddress(LOCAL_ADDRESS)
            .remoteAddress(REMOTE_ADDRESS)
            .clientCertificate(CLIENT_CERTIFICATE)
            .build())
        .build();

    Map<String, Object> result = transformer.fromMessageToErrorResponseParameters(message);

    assertThat(result, is(notNullValue()));
    assertThat(((HttpListenerErrorResponseBuilder) result.get("errorResponse")).getBody().getValue().toString(),
               is(EXPECTED_PAYLOAD));
    assertThat(((HttpListenerErrorResponseBuilder) result.get("errorResponse")).getBody().getDataType().getMediaType().toString(),
               is(EXPECTED_MEDIA_TYPE.toString()));
  }

  @Test
  public void statusCodeIsUpdatedWhenIs0() {
    Message message = Message.builder().value(EXPECTED_PAYLOAD)
        .mediaType(EXPECTED_MEDIA_TYPE)
        .attributesValue(new HttpResponseAttributesBuilder()
            .headers(HEADERS)
            .statusCode(0)
            .build())
        .build();

    Map<String, Object> result = transformer.fromMessageToErrorResponseParameters(message);

    assertThat(result, is(notNullValue()));
    assertThat(((HttpListenerErrorResponseBuilder) result.get("errorResponse")).getStatusCode(), is(500));
  }

  @Test
  public void payloadAndAttributesArePreservedWhenTransformingFromSuccessToMessage() {
    OptionalLong length = OptionalLong.of(10L);
    TypedValue<Object> body = new TypedValue<>(EXPECTED_PAYLOAD, ATOM_STRING, length);
    HttpListenerResponseBuilder httpListenerResponseBuilder = new HttpListenerErrorResponseBuilder();
    httpListenerResponseBuilder.setBody(body);
    httpListenerResponseBuilder.setReasonPhrase(REASON_PHRASE);
    httpListenerResponseBuilder.setStatusCode(STATUS_CODE);
    httpListenerResponseBuilder.setHeaders(HEADERS);
    Map<String, Object> parameters = ImmutableMap.of("response", httpListenerResponseBuilder);

    Message message = transformer.fromSuccessResponseParametersToMessage(parameters);

    assertThat(message, is(notNullValue()));
    assertThat(message.getPayload(), is(body));
    assertThat(message.getAttributes().getValue(), instanceOf(HttpResponseAttributes.class));
    HttpResponseAttributes attributes = (HttpResponseAttributes) message.getAttributes().getValue();
    assertThat(attributes.getReasonPhrase(), is(REASON_PHRASE));
    assertThat(attributes.getStatusCode(), is(STATUS_CODE));
    assertThat(attributes.getHeaders(), is(HEADERS));
  }
}
