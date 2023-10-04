/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api.policy;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.mule.extension.http.api.policy.HttpPolicyResponseAttributes;
import org.mule.runtime.api.util.MultiMap;
import org.mule.test.http.api.AbstractHttpAttributesTestCase;

import org.junit.Before;
import org.junit.Test;

public class HttpPolicyResponseAttributesTestCase extends AbstractHttpAttributesTestCase {

  private static final String COMPLETE_TO_STRING =
      "org.mule.extension.http.api.policy.HttpPolicyResponseAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Status Code=401" + lineSeparator() +
          "   Reason Phrase=Some Reason Phrase" + lineSeparator() +
          "   Headers=[" + lineSeparator() +
          "      header1=headerValue1" + lineSeparator() +
          "      header2=headerValue2" + lineSeparator() +
          "   ]" + lineSeparator() +
          "}";

  private static final String TO_STRING_WITHOUT_STATUS_CODE =
      "org.mule.extension.http.api.policy.HttpPolicyResponseAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Status Code=null" + lineSeparator() +
          "   Reason Phrase=Some Reason Phrase" + lineSeparator() +
          "   Headers=[" + lineSeparator() +
          "      header1=headerValue1" + lineSeparator() +
          "      header2=headerValue2" + lineSeparator() +
          "   ]" + lineSeparator() +
          "}";


  private static final String TO_STRING_WITHOUT_HEADERS =
      "org.mule.extension.http.api.policy.HttpPolicyResponseAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Status Code=401" + lineSeparator() +
          "   Reason Phrase=Some Reason Phrase" + lineSeparator() +
          "   Headers=[]" + lineSeparator() +
          "}";

  private static final String TO_STRING_WITHOUT_REASON_PHRASE =
      "org.mule.extension.http.api.policy.HttpPolicyResponseAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Status Code=401" + lineSeparator() +
          "   Reason Phrase=null" + lineSeparator() +
          "   Headers=[" + lineSeparator() +
          "      header1=headerValue1" + lineSeparator() +
          "      header2=headerValue2" + lineSeparator() +
          "   ]" + lineSeparator() +
          "}";

  private static final String OBFUSCATED_TO_STRING =
      "org.mule.extension.http.api.policy.HttpPolicyResponseAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Status Code=401" + lineSeparator() +
          "   Reason Phrase=Some Reason Phrase" + lineSeparator() +
          "   Headers=[" + lineSeparator() +
          "      password=****" + lineSeparator() +
          "      pass=****" + lineSeparator() +
          "      client_secret=****" + lineSeparator() +
          "      authorization=****" + lineSeparator() +
          "      regular=show me" + lineSeparator() +
          "   ]" + lineSeparator() +
          "}";

  private HttpPolicyResponseAttributes responseAttributes;

  @Before
  public void setUp() {
    responseAttributes = new HttpPolicyResponseAttributes();
  }

  @Test
  public void completeToString() {
    setUpHeadersToResponseAttributes(responseAttributes);
    setUpReasonPhraseToResponseAttributes(responseAttributes);
    setUpStatusCodeToResponseAttributes(responseAttributes);

    assertThat(responseAttributes.toString(), is(COMPLETE_TO_STRING));
  }

  @Test
  public void toStringWithoutHeaders() {
    setUpReasonPhraseToResponseAttributes(responseAttributes);
    setUpStatusCodeToResponseAttributes(responseAttributes);

    assertThat(responseAttributes.toString(), is(TO_STRING_WITHOUT_HEADERS));
  }

  @Test
  public void toStringWithoutReasonPrhase() {
    setUpHeadersToResponseAttributes(responseAttributes);
    setUpStatusCodeToResponseAttributes(responseAttributes);

    assertThat(responseAttributes.toString(), is(TO_STRING_WITHOUT_REASON_PHRASE));
  }

  @Test
  public void toStringWithoutStatusCode() {
    setUpHeadersToResponseAttributes(responseAttributes);
    setUpReasonPhraseToResponseAttributes(responseAttributes);

    assertThat(responseAttributes.toString(), is(TO_STRING_WITHOUT_STATUS_CODE));
  }

  @Test
  public void sensitiveContentIsHidden() {
    responseAttributes.setHeaders(prepareSensitiveDataMap(new MultiMap<>()));
    setUpReasonPhraseToResponseAttributes(responseAttributes);
    setUpStatusCodeToResponseAttributes(responseAttributes);

    assertThat(responseAttributes.toString(), is(OBFUSCATED_TO_STRING));
  }

  private void setUpHeadersToResponseAttributes(HttpPolicyResponseAttributes responseAttributes) {
    responseAttributes.setHeaders(getHeaders());
  }

  private void setUpReasonPhraseToResponseAttributes(HttpPolicyResponseAttributes responseAttributes) {
    responseAttributes.setReasonPhrase("Some Reason Phrase");
  }

  private void setUpStatusCodeToResponseAttributes(HttpPolicyResponseAttributes responseAttributes) {
    responseAttributes.setStatusCode(401);
  }
}
