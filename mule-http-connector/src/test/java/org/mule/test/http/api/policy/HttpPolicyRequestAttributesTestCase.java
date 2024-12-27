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

import org.mule.extension.http.api.policy.HttpPolicyRequestAttributes;
import org.mule.runtime.api.util.MultiMap;
import org.mule.test.http.api.AbstractHttpAttributesTestCase;

import java.util.HashMap;

import org.junit.Test;

public class HttpPolicyRequestAttributesTestCase extends AbstractHttpAttributesTestCase {

  private static final String TO_STRING_COMPLETE =
      "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Request path=/request/path" + lineSeparator() +
          "   Headers=[" + lineSeparator() +
          "      header2=headerValue2" + lineSeparator() +
          "      header1=headerValue1" + lineSeparator() +
          "   ]" + lineSeparator() +
          "   Query Parameters=[" + lineSeparator() +
          "      queryParam1=queryParamValue1" + lineSeparator() +
          "      queryParam2=queryParamValue2" + lineSeparator() +
          "   ]" + lineSeparator() +
          "   URI Parameters=[" + lineSeparator() +
          "      uriParam1=uriParamValue1" + lineSeparator() +
          "      uriParam2=uriParamValue2" + lineSeparator() +
          "   ]" + lineSeparator() +
          "}";

  private static final String TO_STRING_EMPTY =
      "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Request path=null" + lineSeparator() +
          "   Headers=[]" + lineSeparator() +
          "   Query Parameters=[]" + lineSeparator() +
          "   URI Parameters=[]" + lineSeparator() +
          "}";

  private static final String TO_STRING_QUERY_PARAMS =
      "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Request path=null" + lineSeparator() +
          "   Headers=[]" + lineSeparator() +
          "   Query Parameters=[" + lineSeparator() +
          "      queryParam1=queryParamValue1" + lineSeparator() +
          "      queryParam2=queryParamValue2" + lineSeparator() +
          "   ]" + lineSeparator() +
          "   URI Parameters=[]" + lineSeparator() +
          "}";

  private static final String TO_STRING_URI_PARAMS =
      "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Request path=null" + lineSeparator() +
          "   Headers=[]" + lineSeparator() +
          "   Query Parameters=[]" + lineSeparator() +
          "   URI Parameters=[" + lineSeparator() +
          "      uriParam1=uriParamValue1" + lineSeparator() +
          "      uriParam2=uriParamValue2" + lineSeparator() +
          "   ]" + lineSeparator() +
          "}";

  private static final String TO_STRING_OBFUSCATED =
      "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes" + lineSeparator() +
          "{" + lineSeparator() +
          "   Request path=/request/path" + lineSeparator() +
          "   Headers=[" + lineSeparator() +
          "      authorization=****" + lineSeparator() +
          "      password=****" + lineSeparator() +
          "      pass=****" + lineSeparator() +
          "      client_secret=****" + lineSeparator() +
          "      regular=show me" + lineSeparator() +
          "   ]" + lineSeparator() +
          "   Query Parameters=[" + lineSeparator() +
          "      password=****" + lineSeparator() +
          "      pass=****" + lineSeparator() +
          "      client_secret=****" + lineSeparator() +
          "      authorization=****" + lineSeparator() +
          "      regular=show me" + lineSeparator() +
          "   ]" + lineSeparator() +
          "   URI Parameters=[" + lineSeparator() +
          "      authorization=****" + lineSeparator() +
          "      password=****" + lineSeparator() +
          "      pass=****" + lineSeparator() +
          "      client_secret=****" + lineSeparator() +
          "      regular=show me" + lineSeparator() +
          "   ]" + lineSeparator() +
          "}";

  private Object requestAttributes;

  @Test
  public void completeToString() {
    requestAttributes = new HttpPolicyRequestAttributes(getHeaders(), getQueryParams(), getUriParams(), "/request/path");
    assertThat(TO_STRING_COMPLETE, is(requestAttributes.toString()));
  }

  @Test
  public void emptyToString() {
    requestAttributes = new HttpPolicyRequestAttributes(new MultiMap<>(), new MultiMap<>(), new HashMap<>(), null);
    assertThat(TO_STRING_EMPTY, is(requestAttributes.toString()));
  }

  @Test
  public void onlyQueryParamToString() {
    requestAttributes = new HttpPolicyRequestAttributes(new MultiMap<>(), getQueryParams(), new HashMap<>(), null);
    assertThat(TO_STRING_QUERY_PARAMS, is(requestAttributes.toString()));
  }

  @Test
  public void onlyUriParamToString() {
    requestAttributes = new HttpPolicyRequestAttributes(new MultiMap<>(), new MultiMap<>(), getUriParams(), null);
    assertThat(TO_STRING_URI_PARAMS, is(requestAttributes.toString()));
  }

  @Test
  public void sensitiveContentIsHidden() {
    MultiMap<String, String> sensitiveDataMultiMap = prepareSensitiveDataMap(new MultiMap<>());
    requestAttributes = new HttpPolicyRequestAttributes(sensitiveDataMultiMap, sensitiveDataMultiMap,
                                                        prepareSensitiveDataMap(new HashMap<>()), "/request/path");
    assertThat(TO_STRING_OBFUSCATED, is(requestAttributes.toString()));
  }

}
