/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Test;

public class HttpResponseAttributesTestCase extends AbstractMuleTestCase {

  private static final String COMPLETE_TO_STRING = "org.mule.extension.http.api.HttpResponseAttributes\n" +
      "{\n" +
      "   Status Code=401\n" +
      "   Reason Phrase=Some Reason Phrase\n" +
      "   Headers=[\n" +
      "      header2=value2\n" +
      "      header1=value1\n" +
      "   ]\n" +
      "}";

  private static final String TO_STRING_WITHOUT_HEADERS = "org.mule.extension.http.api.HttpResponseAttributes\n" +
      "{\n" +
      "   Status Code=401\n" +
      "   Reason Phrase=Some Reason Phrase\n" +
      "   Headers=[]\n" +
      "}";

  private static final String TO_STRING_WITHOUT_REASON_PHRASE = "org.mule.extension.http.api.HttpResponseAttributes\n" +
      "{\n" +
      "   Status Code=401\n" +
      "   Reason Phrase=null\n" +
      "   Headers=[\n" +
      "      header2=value2\n" +
      "      header1=value1\n" +
      "   ]\n" +
      "}";

  private HttpResponseAttributes responseAttributes;

  @Test
  public void completeToString() {
    responseAttributes = new HttpResponseAttributes(401, "Some Reason Phrase", getHeadersToResponseAttributes());

    assertThat(COMPLETE_TO_STRING, is(responseAttributes.toString()));
  }

  @Test
  public void toStringWithoutHeaders() {
    responseAttributes = new HttpResponseAttributes(401, "Some Reason Phrase", new MultiMap<>());

    assertThat(TO_STRING_WITHOUT_HEADERS, is(responseAttributes.toString()));
  }

  @Test
  public void toStringWithoutReasonPrhase() {
    responseAttributes = new HttpResponseAttributes(401, null, getHeadersToResponseAttributes());

    assertThat(TO_STRING_WITHOUT_REASON_PHRASE, is(responseAttributes.toString()));
  }

  private MultiMap<String, String> getHeadersToResponseAttributes() {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("header1", "value1");
    headers.put("header2", "value2");
    return headers;
  }
}
