/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import org.mule.extension.http.api.HttpResponseAttributes;

import io.qameta.allure.Feature;
import org.junit.Test;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;

@Feature(HTTP_EXTENSION)
public class HttpResponseAttributesTestCase extends AbstractHttpAttributesTestCase {

  private static final String COMPLETE_TO_STRING = "org.mule.extension.http.api.HttpResponseAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Status Code=401" + lineSeparator() +
      "   Reason Phrase=Some Reason Phrase" + lineSeparator() +
      "   Headers=[" + lineSeparator() +
      "      header1=headerValue1" + lineSeparator() +
      "      header2=headerValue2" + lineSeparator() +
      "   ]" + lineSeparator() +
      "}";

  private static final String TO_STRING_WITHOUT_HEADERS = "org.mule.extension.http.api.HttpResponseAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Status Code=401" + lineSeparator() +
      "   Reason Phrase=Some Reason Phrase" + lineSeparator() +
      "   Headers=[]" + lineSeparator() +
      "}";

  private static final String TO_STRING_WITHOUT_REASON_PHRASE =
      "org.mule.extension.http.api.HttpResponseAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Status Code=401" + lineSeparator() +
          "   Reason Phrase=null" + lineSeparator() +
          "   Headers=[" + lineSeparator() +
          "      header1=headerValue1" + lineSeparator() +
          "      header2=headerValue2" + lineSeparator() +
          "   ]" + lineSeparator() +
          "}";

  private static final String OBFUSCATED_TO_STRING = "org.mule.extension.http.api.HttpResponseAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Status Code=401" + lineSeparator() +
      "   Reason Phrase=Unauthorised" + lineSeparator() +
      "   Headers=[" + lineSeparator() +
      "      password=****" + lineSeparator() +
      "      pass=****" + lineSeparator() +
      "      client_secret=****" + lineSeparator() +
      "      authorization=****" + lineSeparator() +
      "      regular=show me" + lineSeparator() +
      "   ]" + lineSeparator() +
      "}";

  private HttpResponseAttributes responseAttributes;

  @Test
  public void completeToString() {
    responseAttributes = new HttpResponseAttributes(401, "Some Reason Phrase", getHeaders());

    assertThat(responseAttributes.toString(), is(COMPLETE_TO_STRING));
  }

  @Test
  public void toStringWithoutHeaders() {
    responseAttributes = new HttpResponseAttributes(401, "Some Reason Phrase", new CaseInsensitiveMultiMap());

    assertThat(responseAttributes.toString(), is(TO_STRING_WITHOUT_HEADERS));
  }

  @Test
  public void toStringWithoutReasonPhrase() {
    responseAttributes = new HttpResponseAttributes(401, null, getHeaders());

    assertThat(responseAttributes.toString(), is(TO_STRING_WITHOUT_REASON_PHRASE));
  }

  @Test
  public void sensitiveContentIsHidden() {
    responseAttributes = new HttpResponseAttributes(401, "Unauthorised", prepareSensitiveDataMap(new CaseInsensitiveMultiMap()));

    assertThat(responseAttributes.toString(), is(OBFUSCATED_TO_STRING));
  }

}
