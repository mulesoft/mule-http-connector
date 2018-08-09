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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.junit.Test;

public class HttpRequestAttributesTestCase extends AbstractHttpAttributesTestCase {

  private static final String TO_STRING_COMPLETE = "org.mule.extension.http.api.HttpRequestAttributes\n" +
      "{\n" +
      "   Request path=/request/path\n" +
      "   Method=GET\n" +
      "   Listener path=/listener/path\n" +
      "   Local Address=http://127.0.0.1:8080/\n" +
      "   Query String=queryParam1=queryParam1&queryParam2=queryParam2\n" +
      "   Relative Path=/relative/path\n" +
      "   Masked Request Path=null\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
      "   Headers=[\n" +
      "      header2=headerValue2\n" +
      "      header1=headerValue1\n" +
      "   ]\n" +
      "   Query Parameters=[\n" +
      "      queryParam1=queryParamValue1\n" +
      "      queryParam2=queryParamValue2\n" +
      "   ]\n" +
      "   URI Parameters=[\n" +
      "      uriParam1=uriParamValue1\n" +
      "      uriParam2=uriParamValue2\n" +
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
      "   Masked Request Path=null\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[]\n" +
      "   URI Parameters=[]\n" +
      "}";

  private static final String TO_STRING_EMPTY_WITH_MASKED_REQUEST_PATH = "org.mule.extension.http.api.HttpRequestAttributes\n" +
      "{\n" +
      "   Request path=/request/path/proxy\n" +
      "   Method=GET\n" +
      "   Listener path=/listener/path/*\n" +
      "   Local Address=http://127.0.0.1:8080/\n" +
      "   Query String=\n" +
      "   Relative Path=/relative/path\n" +
      "   Masked Request Path=/proxy\n" +
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
      "   Masked Request Path=null\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[\n" +
      "      queryParam1=queryParamValue1\n" +
      "      queryParam2=queryParamValue2\n" +
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
      "   Masked Request Path=null\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
      "   Headers=[]\n" +
      "   Query Parameters=[]\n" +
      "   URI Parameters=[\n" +
      "      uriParam1=uriParamValue1\n" +
      "      uriParam2=uriParamValue2\n" +
      "   ]\n" +
      "}";

  private static final String TO_STRING_OBFUSCATED = "org.mule.extension.http.api.HttpRequestAttributes\n" +
      "{\n" +
      "   Request path=/request/path\n" +
      "   Method=GET\n" +
      "   Listener path=/listener/path\n" +
      "   Local Address=http://127.0.0.1:8080/\n" +
      "   Query String=****\n" +
      "   Relative Path=/relative/path\n" +
      "   Masked Request Path=null\n" +
      "   Remote Address=http://10.1.2.5:8080/\n" +
      "   Request Uri=http://127.0.0.1/gateway\n" +
      "   Scheme=scheme\n" +
      "   Version=1.0\n" +
      "   Headers=[\n" +
      "      password=****\n" +
      "      pass=****\n" +
      "      client_secret=****\n" +
      "      regular=show me\n" +
      "   ]\n" +
      "   Query Parameters=[\n" +
      "      password=****\n" +
      "      pass=****\n" +
      "      client_secret=****\n" +
      "      regular=show me\n" +
      "   ]\n" +
      "   URI Parameters=[\n" +
      "      password=****\n" +
      "      pass=****\n" +
      "      client_secret=****\n" +
      "      regular=show me\n" +
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

    assertThat(requestAttributes.toString(), is(TO_STRING_COMPLETE));
  }

  @Test
  public void defaultToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    assertThat(baseBuilder.build().toString(), is(TO_STRING_EMPTY));
  }

  @Test
  public void onlyQueryParamToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    assertThat(baseBuilder
        .queryParams(getQueryParams())
        .queryString("queryParam1=queryParam1&queryParam2=queryParam2")
        .build()
        .toString(),
               is(TO_STRING_QUERY_PARAMS));
  }

  @Test
  public void onlyUriParamToString() throws IllegalAccessException, InvocationTargetException, InstantiationException {
    assertThat(baseBuilder.uriParams(getUriParams()).build().toString(), is(TO_STRING_URI_PARAMS));
  }

  @Test
  public void sensitiveContentIsHidden() {
    requestAttributes = baseBuilder
        .headers(prepareSensitiveDataMap(new MultiMap<>()))
        .queryString("password=4n3zP4SSW0rd&pass=s0m3P4zz&client_secret=myPr3c10us&regular=show+me")
        .queryParams(prepareSensitiveDataMap(new MultiMap<>()))
        .uriParams(prepareSensitiveDataMap(new HashMap<>()))
        .build();

    assertThat(requestAttributes.toString(), is(TO_STRING_OBFUSCATED));
  }

  @Test
  public void withMaskedRequestPath() throws Exception {
    assertThat(baseBuilder.listenerPath("/listener/path/*").requestPath("/request/path/proxy").build().toString(),
               is(TO_STRING_EMPTY_WITH_MASKED_REQUEST_PATH));
  }

}
