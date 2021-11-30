/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.util.MultiMap;

import io.qameta.allure.Feature;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Feature(HTTP_EXTENSION)
public class HttpResponseAttributesTestCase extends AbstractHttpAttributesTestCase {

  private static final String COMPLETE_TO_STRING_BEGIN = "org.mule.extension.http.api.HttpResponseAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Status Code=401" + lineSeparator() +
      "   Reason Phrase=Some Reason Phrase" + lineSeparator() +
      "   Headers=[" + lineSeparator();
  private static final Set<String> COMPLETE_TO_STRING_HEADER_LINES =
      new HashSet<String>(asList("header2=headerValue2",
                                 "header1=headerValue1"));
  private static final String COMPLETE_TO_STRING_END =
      "   ]" + lineSeparator() +
          "}";

  private static final String TO_STRING_WITHOUT_HEADERS = "org.mule.extension.http.api.HttpResponseAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Status Code=401" + lineSeparator() +
      "   Reason Phrase=Some Reason Phrase" + lineSeparator() +
      "   Headers=[]" + lineSeparator() +
      "}";

  private static final String TO_STRING_WITHOUT_REASON_PHRASE_BEGIN =
      "org.mule.extension.http.api.HttpResponseAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Status Code=401" + lineSeparator() +
          "   Reason Phrase=null" + lineSeparator() +
          "   Headers=[" + lineSeparator();
  private static final Set<String> TO_STRING_WITHOUT_REASON_PHRASE_HEADER_LINES =
      new HashSet<String>(asList("header2=headerValue2",
                                 "header1=headerValue1"));
  private static final String TO_STRING_WITHOUT_REASON_PHRASE_END =
      "   ]" + lineSeparator() +
          "}";

  private static final String OBFUSCATED_TO_STRING_BEGIN =
      "org.mule.extension.http.api.HttpResponseAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Status Code=401" + lineSeparator() +
          "   Reason Phrase=Unauthorised" + lineSeparator() +
          "   Headers=[" + lineSeparator();
  private static final Set<String> OBFUSCATED_TO_STRING_HEADER_LINES =
      new HashSet<String>(asList("authorization=****",
                                 "password=****",
                                 "pass=****",
                                 "client_secret=****",
                                 "regular=show me"));
  private static final String OBFUSCATED_TO_STRING_END =
      "   ]" + lineSeparator() +
          "}";

  private HttpResponseAttributes responseAttributes;

  @Test
  public void toStringWithoutHeaders() {
    responseAttributes = new HttpResponseAttributes(401, "Some Reason Phrase", new MultiMap<>());
    assertThat(responseAttributes.toString(), is(TO_STRING_WITHOUT_HEADERS));
  }

  @Test
  public void toStringWithoutReasonPhrase() {
    responseAttributes = new HttpResponseAttributes(401, null, getHeaders());
    assertToStringContent(responseAttributes,
                          TO_STRING_WITHOUT_REASON_PHRASE_BEGIN,
                          TO_STRING_WITHOUT_REASON_PHRASE_HEADER_LINES,
                          TO_STRING_WITHOUT_REASON_PHRASE_END);
  }

  @Test
  public void completeToString() {
    responseAttributes = new HttpResponseAttributes(401, "Some Reason Phrase", getHeaders());
    assertToStringContent(responseAttributes,
                          COMPLETE_TO_STRING_BEGIN,
                          COMPLETE_TO_STRING_HEADER_LINES,
                          COMPLETE_TO_STRING_END);
  }

  @Test
  public void sensitiveContentIsHidden() {
    responseAttributes = new HttpResponseAttributes(401, "Unauthorised", prepareSensitiveDataMap(new MultiMap<>()));
    assertToStringContent(responseAttributes,
                          OBFUSCATED_TO_STRING_BEGIN,
                          OBFUSCATED_TO_STRING_HEADER_LINES,
                          OBFUSCATED_TO_STRING_END);
  }

  private static void assertToStringContent(HttpResponseAttributes attributes, String begin, Set<String> headerLines,
                                            String end) {
    String asString = attributes.toString();
    assertThat(asString, startsWith(begin));
    for (String headerLine : headerLines) {
      assertThat(asString, containsString(headerLine));
    }
    assertThat(asString, endsWith(end));
  }
}
