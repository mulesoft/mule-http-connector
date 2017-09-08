/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.listener.builder.HttpListenerErrorResponseBuilder;
import org.mule.extension.http.api.policy.HttpListenerPolicyParametersTransformer;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;

import java.util.Map;

import org.junit.Test;

public class HttpListenerPolicyParametersTransformerTestCase {

  @Test
  public void getErrorResponseBodyWhenSendingHttpRequestAttributes() {
    HttpListenerPolicyParametersTransformer transformer = new HttpListenerPolicyParametersTransformer();
    Message.Builder messageBuilder = (Message.Builder) Message.builder();
    HttpRequestAttributes httpRequestAttributes =
        new HttpRequestAttributes(new MultiMap<String, String>(), "/test", "/", "1", "scheme", "GET", "/test", "", "",
                                  new MultiMap<String, String>(), new MultiMap<String, String>(), "", null);
    messageBuilder.attributesValue(httpRequestAttributes);
    String expectedPayload = "{'message': 'this is the payload'}";
    messageBuilder.value(expectedPayload);
    messageBuilder.mediaType(MediaType.APPLICATION_JSON);
    Map<String, Object> result = transformer.fromMessageToErrorResponseParameters(messageBuilder.build());

    assertNotNull(result);
    assertEquals(expectedPayload, ((HttpListenerErrorResponseBuilder) result.get("errorResponse")).getBody().getValue());
    assertEquals("application/json", ((HttpListenerErrorResponseBuilder) result.get("errorResponse")).getBody().getDataType()
        .getMediaType().toString());
  }
}
