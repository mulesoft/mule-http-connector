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

import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.runtime.api.util.MultiMap;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.junit.Test;

public class HttpRequestAttributesTestCase extends AbstractHttpAttributesTestCase {

  private static final String TO_STRING_COMPLETE = "org.mule.extension.http.api.HttpRequestAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Request path=/request/path" + lineSeparator() +
      "   Method=GET" + lineSeparator() +
      "   Listener path=/listener/path" + lineSeparator() +
      "   Local Address=http://127.0.0.1:8080/" + lineSeparator() +
      "   Query String=queryParam1=queryParam1&queryParam2=queryParam2" + lineSeparator() +
      "   Relative Path=/relative/path" + lineSeparator() +
      "   Remote Address=http://10.1.2.5:8080/" + lineSeparator() +
      "   Request Uri=http://127.0.0.1/gateway" + lineSeparator() +
      "   Scheme=scheme" + lineSeparator() +
      "   Version=1.0" + lineSeparator() +
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

  private static final String TO_STRING_EMPTY = "org.mule.extension.http.api.HttpRequestAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Request path=/request/path" + lineSeparator() +
      "   Method=GET" + lineSeparator() +
      "   Listener path=/listener/path" + lineSeparator() +
      "   Local Address=http://127.0.0.1:8080/" + lineSeparator() +
      "   Query String=" + lineSeparator() +
      "   Relative Path=/relative/path" + lineSeparator() +
      "   Remote Address=http://10.1.2.5:8080/" + lineSeparator() +
      "   Request Uri=http://127.0.0.1/gateway" + lineSeparator() +
      "   Scheme=scheme" + lineSeparator() +
      "   Version=1.0" + lineSeparator() +
      "   Headers=[]" + lineSeparator() +
      "   Query Parameters=[]" + lineSeparator() +
      "   URI Parameters=[]" + lineSeparator() +
      "}";

  private static final String TO_STRING_QUERY_PARAMS = "org.mule.extension.http.api.HttpRequestAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Request path=/request/path" + lineSeparator() +
      "   Method=GET" + lineSeparator() +
      "   Listener path=/listener/path" + lineSeparator() +
      "   Local Address=http://127.0.0.1:8080/" + lineSeparator() +
      "   Query String=queryParam1=queryParam1&queryParam2=queryParam2" + lineSeparator() +
      "   Relative Path=/relative/path" + lineSeparator() +
      "   Remote Address=http://10.1.2.5:8080/" + lineSeparator() +
      "   Request Uri=http://127.0.0.1/gateway" + lineSeparator() +
      "   Scheme=scheme" + lineSeparator() +
      "   Version=1.0" + lineSeparator() +
      "   Headers=[]" + lineSeparator() +
      "   Query Parameters=[" + lineSeparator() +
      "      queryParam1=queryParamValue1" + lineSeparator() +
      "      queryParam2=queryParamValue2" + lineSeparator() +
      "   ]" + lineSeparator() +
      "   URI Parameters=[]" + lineSeparator() +
      "}";

  private static final String TO_STRING_URI_PARAMS = "org.mule.extension.http.api.HttpRequestAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Request path=/request/path" + lineSeparator() +
      "   Method=GET" + lineSeparator() +
      "   Listener path=/listener/path" + lineSeparator() +
      "   Local Address=http://127.0.0.1:8080/" + lineSeparator() +
      "   Query String=" + lineSeparator() +
      "   Relative Path=/relative/path" + lineSeparator() +
      "   Remote Address=http://10.1.2.5:8080/" + lineSeparator() +
      "   Request Uri=http://127.0.0.1/gateway" + lineSeparator() +
      "   Scheme=scheme" + lineSeparator() +
      "   Version=1.0" + lineSeparator() +
      "   Headers=[]" + lineSeparator() +
      "   Query Parameters=[]" + lineSeparator() +
      "   URI Parameters=[" + lineSeparator() +
      "      uriParam1=uriParamValue1" + lineSeparator() +
      "      uriParam2=uriParamValue2" + lineSeparator() +
      "   ]" + lineSeparator() +
      "}";

  private static final String TO_STRING_OBFUSCATED = "org.mule.extension.http.api.HttpRequestAttributes" + lineSeparator() +
      "{" + lineSeparator() +
      "   Request path=/request/path" + lineSeparator() +
      "   Method=GET" + lineSeparator() +
      "   Listener path=/listener/path" + lineSeparator() +
      "   Local Address=http://127.0.0.1:8080/" + lineSeparator() +
      "   Query String=****" + lineSeparator() +
      "   Relative Path=/relative/path" + lineSeparator() +
      "   Remote Address=http://10.1.2.5:8080/" + lineSeparator() +
      "   Request Uri=http://127.0.0.1/gateway" + lineSeparator() +
      "   Scheme=scheme" + lineSeparator() +
      "   Version=1.0" + lineSeparator() +
      "   Headers=[" + lineSeparator() +
      "      password=****" + lineSeparator() +
      "      pass=****" + lineSeparator() +
      "      client_secret=****" + lineSeparator() +
      "      regular=show me" + lineSeparator() +
      "   ]" + lineSeparator() +
      "   Query Parameters=[" + lineSeparator() +
      "      password=****" + lineSeparator() +
      "      pass=****" + lineSeparator() +
      "      client_secret=****" + lineSeparator() +
      "      regular=show me" + lineSeparator() +
      "   ]" + lineSeparator() +
      "   URI Parameters=[" + lineSeparator() +
      "      password=****" + lineSeparator() +
      "      pass=****" + lineSeparator() +
      "      client_secret=****" + lineSeparator() +
      "      regular=show me" + lineSeparator() +
      "   ]" + lineSeparator() +
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

}
