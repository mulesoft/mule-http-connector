/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.util.MultiMap;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
public class HttpResponseAttributesTestCase extends AbstractHttpAttributesTestCase {

  private static final String COMPLETE_TO_STRING = "org.mule.extension.http.api.HttpResponseAttributes\n" +
      "{\n" +
      "   Status Code=401\n" +
      "   Reason Phrase=Some Reason Phrase\n" +
      "   Headers=[\n" +
      "      header2=headerValue2\n" +
      "      header1=headerValue1\n" +
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
      "      header2=headerValue2\n" +
      "      header1=headerValue1\n" +
      "   ]\n" +
      "}";

  private static final String OBFUSCATED_TO_STRING = "org.mule.extension.http.api.HttpResponseAttributes\n" +
      "{\n" +
      "   Status Code=401\n" +
      "   Reason Phrase=Unauthorised\n" +
      "   Headers=[\n" +
      "      password=****\n" +
      "      pass=****\n" +
      "      client_secret=****\n" +
      "      regular=show me\n" +
      "   ]\n" +
      "}";

  private HttpResponseAttributes responseAttributes;

  @Test
  public void completeToString() {
    responseAttributes = new HttpResponseAttributes(401, "Some Reason Phrase", getHeaders());

    assertThat(responseAttributes.toString(), is(COMPLETE_TO_STRING));
  }

  @Test
  public void toStringWithoutHeaders() {
    responseAttributes = new HttpResponseAttributes(401, "Some Reason Phrase", new MultiMap<>());

    assertThat(responseAttributes.toString(), is(TO_STRING_WITHOUT_HEADERS));
  }

  @Test
  public void toStringWithoutReasonPhrase() {
    responseAttributes = new HttpResponseAttributes(401, null, getHeaders());

    assertThat(responseAttributes.toString(), is(TO_STRING_WITHOUT_REASON_PHRASE));
  }

  @Test
  public void sensitiveContentIsHidden() {
    responseAttributes = new HttpResponseAttributes(401, "Unauthorised", prepareSensitiveDataMap(new MultiMap<>()));

    assertThat(responseAttributes.toString(), is(OBFUSCATED_TO_STRING));
  }

}
