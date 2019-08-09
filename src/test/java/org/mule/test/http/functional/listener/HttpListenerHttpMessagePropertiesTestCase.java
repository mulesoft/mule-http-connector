/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.client.fluent.Request.Post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.api.config.MuleProperties.MULE_CORRELATION_ID_PROPERTY;
import static org.mule.runtime.http.api.HttpHeaders.Names.X_CORRELATION_ID;
import static org.mule.runtime.http.api.HttpHeaders.Names.X_FORWARDED_FOR;
import static org.mule.runtime.http.api.domain.HttpProtocol.HTTP_1_1;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.functional.api.component.TestConnectorQueueHandler;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.http.api.domain.HttpProtocol;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

public class HttpListenerHttpMessagePropertiesTestCase extends AbstractHttpTestCase {

  public static final String QUERY_PARAM_NAME = "queryParam";
  public static final String QUERY_PARAM_VALUE = "paramValue";
  public static final String QUERY_PARAM_VALUE_WITH_SPACES = "param Value";
  public static final String QUERY_PARAM_SECOND_VALUE = "paramAnotherValue";
  public static final String SECOND_QUERY_PARAM_NAME = "queryParam2";
  public static final String SECOND_QUERY_PARAM_VALUE = "paramValue2";
  public static final String CONTEXT_PATH = "/context/path";
  public static final String CONTEXT_ENCODED_PATH = "/context%20path%25";
  public static final String API_CONTEXT_PATH = "/api" + CONTEXT_PATH;
  public static final String API_CONTEXT_ENCODED_PATH = "/a%20p%20i" + CONTEXT_ENCODED_PATH;
  public static final String BASE_PATH = "/";

  private static final String FIRST_URI_PARAM_NAME = "uri-param1";
  private static final String SECOND_URI_PARAM_NAME = "uri-param2";
  private static final String THIRD_URI_PARAM_NAME = "uri-param3";
  public static final String FIRST_URI_PARAM = "uri-param-value-1";
  public static final String SECOND_URI_PARAM_VALUE = "uri-param-value-2";
  public static final String THIRD_URI_PARAM_VALUE = "uri-param-value-3";

  private TestConnectorQueueHandler queueHandler;

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    queueHandler = new TestConnectorQueueHandler(registry);
  }

  @Rule
  public DynamicPort listenPort = new DynamicPort("port1");

  @Rule
  public DynamicPort listenBasePort = new DynamicPort("port2");

  @Rule
  public DynamicPort listenEncodedBasePort = new DynamicPort("port3");

  @Override
  protected String getConfigFile() {
    return "http-listener-message-properties-config.xml";
  }

  @Test
  public void get() throws Exception {
    final String url = format("http://localhost:%s", listenPort.getNumber());
    Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes attributes = getAttributes(message);
    assertThat(attributes.getRequestUri(), is(BASE_PATH));
    assertThat(attributes.getRequestPath(), is(BASE_PATH));
    assertThat(attributes.getRelativePath(), is(BASE_PATH));
    assertThat(attributes.getQueryString(), is(""));
    assertThat(attributes.getUriParams(), notNullValue());
    assertThat(attributes.getUriParams().isEmpty(), is(true));
    final Map queryParams = attributes.getQueryParams();
    assertThat(queryParams, notNullValue());
    assertThat(queryParams.size(), is(0));
    assertThat(attributes.getMethod(), is("GET"));
    assertThat(attributes.getVersion(), is(HTTP_1_1.asString()));
    assertThat(attributes.getLocalAddress(), containsString("/127.0.0.1"));
    assertThat(attributes.getRemoteAddress(), is(startsWith("/127.0.0.1")));
  }

  @Test
  public void getWithQueryParams() throws Exception {
    final ImmutableMap<String, Object> queryParams = ImmutableMap.<String, Object>builder()
        .put(QUERY_PARAM_NAME, QUERY_PARAM_VALUE).put(SECOND_QUERY_PARAM_NAME, SECOND_QUERY_PARAM_VALUE).build();
    final String uri = "/?" + buildQueryString(queryParams);
    final String url = format("http://localhost:%s" + uri, listenPort.getNumber());
    Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes attributes = getAttributes(message);
    assertThat(attributes.getRequestUri(), is(uri));
    assertThat(attributes.getRequestPath(), is(BASE_PATH));
    assertThat(attributes.getRelativePath(), is(BASE_PATH));
    Map<String, String> retrivedQueryParams = attributes.getQueryParams();
    assertThat(retrivedQueryParams, notNullValue());
    assertThat(retrivedQueryParams.size(), is(2));
    assertThat(retrivedQueryParams.get(QUERY_PARAM_NAME), is(QUERY_PARAM_VALUE));
    assertThat(retrivedQueryParams.get(SECOND_QUERY_PARAM_NAME), is(SECOND_QUERY_PARAM_VALUE));
  }

  @Test
  public void getWithQueryParamMultipleValues() throws Exception {
    final ImmutableMap<String, Object> queryParams = ImmutableMap.<String, Object>builder()
        .put(QUERY_PARAM_NAME, Arrays.asList(QUERY_PARAM_VALUE, QUERY_PARAM_SECOND_VALUE)).build();
    final String url = format("http://localhost:%s/?" + buildQueryString(queryParams), listenPort.getNumber());
    Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes attributes = getAttributes(message);
    MultiMap<String, String> retrivedQueryParams = attributes.getQueryParams();
    assertThat(retrivedQueryParams, notNullValue());
    assertThat(retrivedQueryParams.size(), is(2));
    assertThat(retrivedQueryParams.get(QUERY_PARAM_NAME), is(QUERY_PARAM_VALUE));
    assertThat(retrivedQueryParams.getAll(QUERY_PARAM_NAME).size(), is(2));
    assertThat(retrivedQueryParams.getAll(QUERY_PARAM_NAME),
               Matchers.containsInAnyOrder(new String[] {QUERY_PARAM_VALUE, QUERY_PARAM_SECOND_VALUE}));
  }

  @Test
  public void postWithEncodedValues() throws Exception {
    final ImmutableMap<String, Object> queryParams =
        ImmutableMap.<String, Object>builder()
            .put(QUERY_PARAM_NAME, QUERY_PARAM_VALUE_WITH_SPACES)
            .put("encoded", "%")
            .put("%24", "encodeMe")
            .build();
    final String url = format("http://localhost:%s/%s?", listenPort.getNumber(), "a%20path%25") + buildQueryString(queryParams);
    Post(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes attributes = getAttributes(message);
    assertThat(attributes.getListenerPath(), is("/*"));
    assertThat(attributes.getRequestPath(), is("/a path%"));
    assertThat(attributes.getRawRequestPath(), is("/a%20path%25"));
    assertThat(attributes.getRequestUri(), is("/a path%?queryParam=param+Value&encoded=%&%24=encodeMe"));
    assertThat(attributes.getRawRequestUri(), is("/a%20path%25?queryParam=param+Value&encoded=%25&%2524=encodeMe"));
    assertThat(attributes.getQueryString(), is("queryParam=param+Value&encoded=%&%24=encodeMe"));
    MultiMap<String, String> retrivedQueryParams = attributes.getQueryParams();
    assertThat(retrivedQueryParams, notNullValue());
    assertThat(retrivedQueryParams.size(), is(3));
    assertThat(retrivedQueryParams.get(QUERY_PARAM_NAME), is(QUERY_PARAM_VALUE_WITH_SPACES));
    assertThat(retrivedQueryParams.get("%24"), is("encodeMe"));
    assertThat(retrivedQueryParams.get("encoded"), is("%"));
  }

  @Test
  public void putWithOldProtocol() throws Exception {
    final ImmutableMap<String, Object> queryParams =
        ImmutableMap.<String, Object>builder().put(QUERY_PARAM_NAME, Arrays.asList(QUERY_PARAM_VALUE, QUERY_PARAM_VALUE)).build();
    final String url = format("http://localhost:%s/?" + buildQueryString(queryParams), listenPort.getNumber());
    Request.Put(url).version(HttpVersion.HTTP_1_0).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes attributes = getAttributes(message);
    assertThat(attributes.getMethod(), is("PUT"));
    assertThat(attributes.getVersion(), is(HttpProtocol.HTTP_1_0.asString()));
  }

  @Test
  public void getFullUriAndPath() throws Exception {
    final String url = format("http://localhost:%s%s", listenPort.getNumber(), CONTEXT_PATH);
    Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes attributes = getAttributes(message);
    assertThat(attributes.getRequestUri(), is(CONTEXT_PATH));
    assertThat(attributes.getRequestPath(), is(CONTEXT_PATH));
    assertThat(attributes.getRelativePath(), is(CONTEXT_PATH));
  }

  @Test
  public void getAllUriParams() throws Exception {
    final String url = format("http://localhost:%s/%s/%s/%s", listenPort.getNumber(), FIRST_URI_PARAM,
                              SECOND_URI_PARAM_VALUE, THIRD_URI_PARAM_VALUE);
    Post(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    Map<String, String> uriParams = getAttributes(message).getUriParams();
    assertThat(uriParams, notNullValue());
    assertThat(uriParams.size(), is(3));
    assertThat(uriParams.get(FIRST_URI_PARAM_NAME), is(FIRST_URI_PARAM));
    assertThat(uriParams.get(SECOND_URI_PARAM_NAME), is(SECOND_URI_PARAM_VALUE));
    assertThat(uriParams.get(THIRD_URI_PARAM_NAME), is(THIRD_URI_PARAM_VALUE));
  }

  @Test
  public void getUriParamInTheMiddle() throws Exception {
    final String url = format("http://localhost:%s/some-path/%s/some-other-path", listenPort.getNumber(), FIRST_URI_PARAM);
    Post(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    Map<String, String> uriParams = getAttributes(message).getUriParams();
    assertThat(uriParams, notNullValue());
    assertThat(uriParams.size(), is(1));
    assertThat(uriParams.get(FIRST_URI_PARAM_NAME), is(FIRST_URI_PARAM));
  }

  @Test
  public void postUriParamEncoded() throws Exception {
    final String uriParamValue = "uri param value %24";
    final String uriParamValueEncoded = encode(uriParamValue);
    final String url =
        format("http://localhost:%s/some-path/%s/some-other-path", listenPort.getNumber(), uriParamValueEncoded);
    Post(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    Map<String, String> uriParams = getAttributes(message).getUriParams();
    assertThat(uriParams, notNullValue());
    assertThat(uriParams.size(), is(1));
    assertThat(uriParams.get(FIRST_URI_PARAM_NAME), is(uriParamValue));
  }

  @Test
  public void xForwardedForHeader() throws Exception {
    final String url = format("http://localhost:%s/some-path", listenPort.getNumber());

    Post(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes attributes = getAttributes(message);
    assertThat(attributes.getRemoteAddress(), startsWith("/127.0.0.1:"));
    assertThat(attributes.getHeaders().get(X_FORWARDED_FOR), nullValue());

    Post(url).addHeader(X_FORWARDED_FOR, "clientIp, proxy1Ip").connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message forwardedMessage = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes forwardedAttributes = getAttributes(forwardedMessage);
    assertThat(forwardedAttributes.getRemoteAddress(), startsWith("/127.0.0.1:"));
    assertThat(forwardedAttributes.getHeaders().get(X_FORWARDED_FOR.toLowerCase()), is("clientIp, proxy1Ip"));
  }

  @Test
  public void xCorrelationIdHeader() throws Exception {
    final String url = format("http://localhost:%s/some-path", listenPort.getNumber());

    final String myCorrelationId = "myCorrelationId";
    Post(url).addHeader(X_CORRELATION_ID, myCorrelationId).connectTimeout(RECEIVE_TIMEOUT).execute();
    final CoreEvent event = queueHandler.read("out", RECEIVE_TIMEOUT);
    assertThat(event.getCorrelationId(), is(myCorrelationId));
    HttpRequestAttributes attributes = getAttributes(event.getMessage());
    assertThat(attributes.getHeaders().get(X_CORRELATION_ID.toLowerCase()), is(myCorrelationId));
  }

  @Test
  public void muleCorrelationIdHeader() throws Exception {
    final String url = format("http://localhost:%s/some-path", listenPort.getNumber());

    final String myCorrelationId = "myCorrelationId";
    Post(url).addHeader(MULE_CORRELATION_ID_PROPERTY, myCorrelationId).connectTimeout(RECEIVE_TIMEOUT).execute();
    final CoreEvent event = queueHandler.read("out", RECEIVE_TIMEOUT);
    assertThat(event.getCorrelationId(), is(myCorrelationId));
    HttpRequestAttributes attributes = getAttributes(event.getMessage());
    assertThat(attributes.getHeaders().get(MULE_CORRELATION_ID_PROPERTY.toLowerCase()), is(myCorrelationId));
  }

  @Test
  public void xOverridesMuleCorrelationIdHeader() throws Exception {
    final String url = format("http://localhost:%s/some-path", listenPort.getNumber());

    final String myCorrelationId = "myCorrelationId";
    final String myOtherCorrelationId = "myOtherCorrelationId";
    Post(url)
        .addHeader(X_CORRELATION_ID, myCorrelationId)
        .addHeader(MULE_CORRELATION_ID_PROPERTY, myOtherCorrelationId)
        .connectTimeout(RECEIVE_TIMEOUT)
        .execute();
    final CoreEvent event = queueHandler.read("out", RECEIVE_TIMEOUT);
    assertThat(event.getCorrelationId(), is(myCorrelationId));
    HttpRequestAttributes attributes = getAttributes(event.getMessage());
    assertThat(attributes.getHeaders().get(X_CORRELATION_ID.toLowerCase()), is(myCorrelationId));
    assertThat(attributes.getHeaders().get(MULE_CORRELATION_ID_PROPERTY.toLowerCase()), is(myOtherCorrelationId));
  }

  @Test
  public void getBasePath() throws Exception {
    checkBasePath(listenBasePort, API_CONTEXT_PATH, "/api/*", API_CONTEXT_PATH, CONTEXT_PATH, CONTEXT_PATH);
  }

  @Test
  public void getBasePathEncoded() throws Exception {
    checkBasePath(listenEncodedBasePort, API_CONTEXT_ENCODED_PATH, "/a p i/*", "/a p i/context path%", "/context path%",
                  "/context%20path%25");
  }

  private void checkBasePath(DynamicPort listenEncodedBasePort, String apiContextEncodedPath, String expectedListenerPath,
                             String expectedRequestPath, String expectedRelativePath, String expectedMaskedPath)
      throws IOException {
    final String url = format("http://localhost:%s%s", listenEncodedBasePort.getNumber(), apiContextEncodedPath);
    Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    final Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();
    HttpRequestAttributes attributes = getAttributes(message);
    assertThat(attributes.getListenerPath(), is(expectedListenerPath));
    assertThat(attributes.getRequestPath(), is(expectedRequestPath));
    assertThat(attributes.getRelativePath(), is(expectedRelativePath));
    assertThat(attributes.getMaskedRequestPath(), is(expectedMaskedPath));
    Map<String, String> uriParams = attributes.getUriParams();
    assertThat(uriParams, notNullValue());
    assertThat(uriParams.isEmpty(), is(true));
  }

  public HttpRequestAttributes getAttributes(Message message) {
    assertThat(message.getPayload().getValue(), is(instanceOf(HttpRequestAttributes.class)));
    return (HttpRequestAttributes) message.getPayload().getValue();
  }

  public String buildQueryString(Map<String, Object> queryParams) throws UnsupportedEncodingException {
    final StringBuilder queryString = new StringBuilder();
    for (String paramName : queryParams.keySet()) {
      final Object value = queryParams.get(paramName);
      if (value instanceof Collection) {
        for (java.lang.Object eachValue : (Collection) value) {
          queryString.append(encode(paramName) + "=" + encode(eachValue));
          queryString.append("&");
        }
      } else {
        queryString.append(encode(paramName) + "=" + encode(value));
        queryString.append("&");
      }
    }
    queryString.deleteCharAt(queryString.length() - 1);
    return queryString.toString();
  }

  private String encode(Object value) throws UnsupportedEncodingException {
    return URLEncoder.encode(value.toString(), UTF_8.name());
  }

}
