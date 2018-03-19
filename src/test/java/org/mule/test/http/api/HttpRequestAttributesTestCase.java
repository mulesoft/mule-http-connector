/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class HttpRequestAttributesTestCase extends AbstractMuleTestCase {

  private static final String TO_STRING_COMPLETE = "org.mule.extension.http.api.HttpRequestAttributes\n" +
      "{\n" +
      "   Request path=/request/path\n" +
      "   Method=GET\n" +
      "   Listener path=/listener/path\n" +
      "   Local Address=http://127.0.0.1:8080/\n" +
      "   Query String=queryParam1=queryParam1&queryParam2=queryParam2\n" +
      "   Relative Path=/relative/path\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
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

  private static final String TO_STRING_EMPTY = "org.mule.extension.http.api.HttpRequestAttributes\n" +
      "{\n" +
      "   Request path=/request/path\n" +
      "   Method=GET\n" +
      "   Listener path=/listener/path\n" +
      "   Local Address=http://127.0.0.1:8080/\n" +
      "   Query String=\n" +
      "   Relative Path=/relative/path\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[]\n" +
      "   URI Parameters=[]\n" +
      "}";

  private static final String TO_STRING_QUERY_PARAMS = "org.mule.extension.http.api.HttpRequestAttributes\n" +
      "{\n" +
      "   Request path=/request/path\n" +
      "   Method=GET\n" +
      "   Listener path=/listener/path\n" +
      "   Local Address=http://127.0.0.1:8080/\n" +
      "   Query String=queryParam1=queryParam1&queryParam2=queryParam2\n" +
      "   Relative Path=/relative/path\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[\n" +
      "      queryParam1=queryParam1\n" +
      "      queryParam2=queryParam2\n" +
      "   ]\n" +
      "   URI Parameters=[]\n" +
      "}";

  private static final String TO_STRING_URI_PARAMS = "org.mule.extension.http.api.HttpRequestAttributes\n" +
      "{\n" +
      "   Request path=/request/path\n" +
      "   Method=GET\n" +
      "   Listener path=/listener/path\n" +
      "   Local Address=http://127.0.0.1:8080/\n" +
      "   Query String=\n" +
      "   Relative Path=/relative/path\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[]\n" +
      "   URI Parameters=[\n" +
      "      uriParam1=uriParam1\n" +
      "      uriParam2=uriParam2\n" +
      "   ]\n" +
      "}";

  private HttpRequestAttributesBuilder baseBuilder = new HttpRequestAttributesBuilder()
      .listenerPath("/listener/path")
      .relativePath("/relative/path")
      .version("1.0")
      .scheme("scheme")
      .method("GET")
      .requestPath("/request/path")
      .remoteAddress("http://10.1.2.5:8080/")
      .localAddress("http://127.0.0.1:8080/")
      .requestUri("http://127.0.0.1/gateway");

  private Object requestAttributes;

  @Test
  public void completeToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    requestAttributes = baseBuilder
        .headers(getHeaders())
        .queryString("queryParam1=queryParam1&queryParam2=queryParam2")
        .queryParams(getQueryParams())
        .uriParams(getUriParams())
        .build();

    assertThat(TO_STRING_COMPLETE, is(requestAttributes.toString()));
  }

  @Test
  public void defaultToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    assertThat(TO_STRING_EMPTY, is(baseBuilder.build().toString()));
  }

  @Test
  public void onlyQueryParamToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    assertThat(TO_STRING_QUERY_PARAMS, is(baseBuilder
        .queryParams(getQueryParams())
        .queryString("queryParam1=queryParam1&queryParam2=queryParam2")
        .build()
        .toString()));
  }

  @Test
  public void onlyUriParamToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    assertThat(TO_STRING_URI_PARAMS, is(baseBuilder.uriParams(getUriParams()).build().toString()));
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
