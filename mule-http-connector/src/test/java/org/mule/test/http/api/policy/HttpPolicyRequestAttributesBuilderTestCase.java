/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api.policy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import org.mule.extension.http.api.policy.HttpPolicyRequestAttributes;
import org.mule.extension.http.api.policy.HttpPolicyRequestAttributesBuilder;
import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.HashMap;
import java.util.Map;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
public class HttpPolicyRequestAttributesBuilderTestCase extends AbstractMuleTestCase {

  private static final String PATH = "requestPath";

  private HttpPolicyRequestAttributesBuilder builder = new HttpPolicyRequestAttributesBuilder();

  @Test
  public void setAttributes() {
    builder.headers(headers())
        .queryParams(queryParams())
        .uriParams(uriParams())
        .requestPath(PATH);

    HttpPolicyRequestAttributes attributes = builder.build();

    assertThat(attributes.getHeaders(), is(headers()));
    assertThat(attributes.getQueryParams(), is(queryParams()));
    assertThat(attributes.getUriParams(), is(uriParams()));
    assertThat(attributes.getRequestPath(), is(PATH));
  }

  @Test
  public void copyAttributes() {
    HttpPolicyRequestAttributes attributes = new HttpPolicyRequestAttributes(headers(), queryParams(), uriParams(), PATH);

    HttpPolicyRequestAttributes copy = new HttpPolicyRequestAttributesBuilder(attributes).build();

    assertThat(copy.getHeaders(), is(headers()));
    assertThat(copy.getQueryParams(), is(queryParams()));
    assertThat(copy.getUriParams(), is(uriParams()));
    assertThat(copy.getRequestPath(), is(PATH));
  }

  private MultiMap<String, String> headers() {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("headerKey", "headerValue");
    return headers;
  }

  private MultiMap<String, String> queryParams() {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("queryParamKey", "queryParamValue");
    return headers;
  }

  private Map<String, String> uriParams() {
    Map<String, String> headers = new HashMap<>();
    headers.put("uriParamKey", "uriParamValue");
    return headers;
  }

}
