/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.HttpResponseAttributesBuilder;
import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
public class HttpResponseAttributesBuilderTestCase extends AbstractMuleTestCase {

  private static final String REASON = "reasonPhrase";

  private HttpResponseAttributesBuilder builder = new HttpResponseAttributesBuilder();

  @Test
  public void setAttributes() {
    builder.headers(headers())
        .reasonPhrase(REASON)
        .statusCode(OK.getStatusCode());

    HttpResponseAttributes attributes = builder.build();

    assertThat(attributes.getStatusCode(), is(OK.getStatusCode()));
    assertThat(attributes.getReasonPhrase(), is(REASON));
    assertThat(attributes.getHeaders(), is(headers()));
  }

  @Test
  public void copyAttributes() {
    HttpResponseAttributes attributes = new HttpResponseAttributes(OK.getStatusCode(), REASON, headers());

    HttpResponseAttributes copy = new HttpResponseAttributesBuilder(attributes).build();

    assertThat(copy.getStatusCode(), is(OK.getStatusCode()));
    assertThat(copy.getReasonPhrase(), is(REASON));
    assertThat(copy.getHeaders(), is(headers()));
  }

  private MultiMap<String, String> headers() {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("headerKey", "headerValue");
    return headers;
  }

}
