/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api.policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.mule.extension.http.api.policy.HttpPolicyRequestAttributes;
import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class HttpPolicyRequestAttributesTestCae extends AbstractMuleTestCase {

  private static final String TO_STRING_COMPLETE = "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes\n" +
      "{\n" +
      "   Request path=/request/path\n" +
      "   Headers=[\n" +
      "      header2=header2\n" +
      "      header1=header1\n" +
      "   ]\n" +
      "   Query Parameters=[\n" +
      "      queryParam1=queryParam1\n" +
      "      queryParam2=queryParam2\n" +
      "   ]\n" +
      "   URI Parameters=[\n" +
      "      uriParam1=uriParam1\n" +
      "      uriParam2=uriParam2\n" +
      "   ]\n" +
      "}";

  private static final String TO_STRING_EMPTY = "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes\n" +
      "{\n" +
      "   Request path=\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[]\n" +
      "   URI Parameters=[]\n" +
      "}";

  private static final String TO_STRING_QUERY_PARAMS = "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes\n" +
      "{\n" +
      "   Request path=null\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[\n" +
      "      queryParam1=queryParam1\n" +
      "      queryParam2=queryParam2\n" +
      "   ]\n" +
      "   URI Parameters=[]\n" +
      "}";

  private static final String TO_STRING_URI_PARAMS = "org.mule.extension.http.api.policy.HttpPolicyRequestAttributes\n" +
      "{\n" +
      "   Request path=null\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[]\n" +
      "   URI Parameters=[\n" +
      "      uriParam1=uriParam1\n" +
      "      uriParam2=uriParam2\n" +
      "   ]\n" +
      "}";

  private Object requestAttributes;

  @Test
  public void completeToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    requestAttributes = new HttpPolicyRequestAttributes(getHeaders(), getQueryParams(), getUriParams(), "/request/path");
    assertThat(TO_STRING_COMPLETE, is(requestAttributes.toString()));
  }

  @Test
  public void emptyToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    requestAttributes = new HttpPolicyRequestAttributes();
    assertThat(TO_STRING_EMPTY, is(requestAttributes.toString()));
  }

  @Test
  public void onlyQueryParamToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    requestAttributes = new HttpPolicyRequestAttributes(new MultiMap<>(), getQueryParams(), null, null);
    assertThat(TO_STRING_QUERY_PARAMS, is(requestAttributes.toString()));
  }

  @Test
  public void onlyUriParamToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    requestAttributes = new HttpPolicyRequestAttributes(new MultiMap<>(), null, getUriParams(), null);
    assertThat(TO_STRING_URI_PARAMS, is(requestAttributes.toString()));
  }

  private MultiMap<String, String> getHeaders() {
    MultiMap headers = new MultiMap();
    headers.put("header1", "header1");
    headers.put("header2", "header2");
    return headers;
  }

  private MultiMap<String, String> getQueryParams() {
    MultiMap queryParams = new MultiMap();
    queryParams.put("queryParam1", "queryParam1");
    queryParams.put("queryParam2", "queryParam2");
    return queryParams;
  }

  private MultiMap<String, String> getUriParams() {
    MultiMap uriParams = new MultiMap();
    uriParams.put("uriParam1", "uriParam1");
    uriParams.put("uriParam2", "uriParam2");
    return uriParams;
  }
}
