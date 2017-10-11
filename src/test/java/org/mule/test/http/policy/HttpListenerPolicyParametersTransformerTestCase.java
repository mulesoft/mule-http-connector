/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.POLICY_SUPPORT;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.listener.builder.HttpListenerErrorResponseBuilder;
import org.mule.extension.http.api.policy.HttpListenerPolicyParametersTransformer;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.security.cert.Certificate;
import java.util.Map;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
@Story(POLICY_SUPPORT)
public class HttpListenerPolicyParametersTransformerTestCase extends AbstractMuleTestCase {

  private final String EXPECTED_PAYLOAD = "{'message': 'this is the payload'}";
  private final MediaType EXPECTED_MEDIA_TYPE = MediaType.APPLICATION_JSON;

  //HttpRequestAttributes parameters
  private final MultiMap<String, String> HEADERS = new MultiMap<>();
  private final String LISTENER_PATH = "/test";
  private final String RELATIVE_PATH = "/";
  private final String VERSION = "1";
  private final String SCHEME = "scheme";
  private final String METHOD = "GET";
  private final String REQUEST_PATH = "/test";
  private final String REQUEST_URI = "";
  private final String QUERY_STRING = "";
  private final MultiMap<String, String> QUERY_PARAMS = new MultiMap<>();
  private final MultiMap<String, String> URI_PARAMS = new MultiMap<>();
  private final String REMOTE_ADDRESS = "";
  private final Certificate CLIENT_CERTIFICATE = null;

  @Test
  public void getErrorResponseBodyWhenSendingHttpRequestAttributes() {
    HttpListenerPolicyParametersTransformer transformer = new HttpListenerPolicyParametersTransformer();
    Message message = Message.builder().value(EXPECTED_PAYLOAD)
        .mediaType(EXPECTED_MEDIA_TYPE)
        .attributesValue(new HttpRequestAttributes(HEADERS, LISTENER_PATH, RELATIVE_PATH, VERSION, SCHEME, METHOD, REQUEST_PATH,
                                                   REQUEST_URI, QUERY_STRING, QUERY_PARAMS, URI_PARAMS, REMOTE_ADDRESS,
                                                   CLIENT_CERTIFICATE))
        .build();

    Map<String, Object> result = transformer.fromMessageToErrorResponseParameters(message);

    assertThat(result, is(notNullValue()));
    assertThat(((HttpListenerErrorResponseBuilder) result.get("errorResponse")).getBody().getValue().toString(),
               is(EXPECTED_PAYLOAD));
    assertThat(((HttpListenerErrorResponseBuilder) result.get("errorResponse")).getBody().getDataType().getMediaType().toString(),
               is(EXPECTED_MEDIA_TYPE.toString()));
  }
}
