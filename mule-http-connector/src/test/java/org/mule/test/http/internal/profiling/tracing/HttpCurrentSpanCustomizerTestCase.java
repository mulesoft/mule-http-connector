/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.internal.profiling.tracing;


import static org.mule.extension.http.api.HttpHeaders.Names.USER_AGENT;
import static org.mule.extension.http.internal.listener.profiling.tracing.HttpListenerCurrentSpanCustomizer.HTTP_ROUTE;
import static org.mule.extension.http.internal.listener.profiling.tracing.HttpListenerCurrentSpanCustomizer.HTTP_SCHEME;
import static org.mule.extension.http.internal.listener.profiling.tracing.HttpListenerCurrentSpanCustomizer.HTTP_TARGET;
import static org.mule.extension.http.internal.listener.profiling.tracing.HttpListenerCurrentSpanCustomizer.HTTP_USER_AGENT;
import static org.mule.extension.http.internal.listener.profiling.tracing.HttpListenerCurrentSpanCustomizer.NET_HOST_NAME;
import static org.mule.extension.http.internal.listener.profiling.tracing.HttpListenerCurrentSpanCustomizer.NET_HOST_PORT;
import static org.mule.extension.http.internal.listener.profiling.tracing.HttpListenerCurrentSpanCustomizer.getHttpListenerCurrentSpanCustomizer;
import static org.mule.extension.http.internal.request.profiling.tracing.HttpCurrentSpanCustomizer.HTTP_FLAVOR;
import static org.mule.extension.http.internal.request.profiling.tracing.HttpCurrentSpanCustomizer.HTTP_METHOD;
import static org.mule.extension.http.internal.request.profiling.tracing.HttpRequestCurrentSpanCustomizer.HTTP_URL;
import static org.mule.extension.http.internal.request.profiling.tracing.HttpRequestCurrentSpanCustomizer.NET_PEER_NAME;
import static org.mule.extension.http.internal.request.profiling.tracing.HttpRequestCurrentSpanCustomizer.NET_PEER_PORT;
import static org.mule.extension.http.internal.request.profiling.tracing.HttpRequestCurrentSpanCustomizer.getHttpRequesterCurrentSpanCustomizer;
import static org.mule.sdk.api.http.domain.HttpProtocol.HTTP_1_1;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.TRACING;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.internal.request.profiling.tracing.HttpCurrentSpanCustomizer;
import org.mule.runtime.api.util.MultiMap;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;

import io.qameta.allure.Feature;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

@Feature(HTTP_EXTENSION)
@Story(TRACING)
@Issue("W-11818702")
public class HttpCurrentSpanCustomizerTestCase {

  public static final String TEST_HOST = "test.host";
  public static final String HTTPS = "https";
  public static final String LISTENER_PATH = "/listenerPath";
  public static final String EXPECTED_USER_AGENT = "userAgent";
  public static final String LOCAL_ADDRESS = "/localAddress:8080";
  public static final String EXPECTED_METHOD = "GET";
  public static final String EXPECTED_PROTOCOL_VERSION = "1.1";
  public static final String EXPECTED_PORT = "8080";
  public static final String EXPECTED_PEER_NAME = "www.expectedhost.com";
  public static final int TEST_PORT = 8080;
  public static final String EXPECTED_SCHEME = HTTPS;

  @Test
  @Description("The listener span customizer informs the distributed trace context manager the correct attributes/name")
  public void listenerSpan() {
    HttpRequestAttributes attributes = mock(HttpRequestAttributes.class);
    when(attributes.getMethod()).thenReturn("GET");
    when(attributes.getScheme()).thenReturn(HTTPS);
    when(attributes.getVersion()).thenReturn(HTTP_1_1.asString());
    when(attributes.getListenerPath()).thenReturn(LISTENER_PATH);
    MultiMap<String, String> headers = mock(MultiMap.class);
    when(attributes.getHeaders()).thenReturn(headers);
    when(headers.get(USER_AGENT)).thenReturn(EXPECTED_USER_AGENT);
    when(attributes.getLocalAddress()).thenReturn(LOCAL_ADDRESS);

    HttpCurrentSpanCustomizer currentSpanCustomizer =
        getHttpListenerCurrentSpanCustomizer(attributes, TEST_HOST, TEST_PORT);
    DistributedTraceContextManager distributedTraceContextManager = mock(DistributedTraceContextManager.class);
    currentSpanCustomizer.customizeSpan(distributedTraceContextManager);

    verify(distributedTraceContextManager).setCurrentSpanName(EXPECTED_METHOD + " " + LISTENER_PATH);

    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_METHOD, "GET");
    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_FLAVOR, EXPECTED_PROTOCOL_VERSION);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_TARGET, LISTENER_PATH);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(NET_HOST_NAME, TEST_HOST);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(NET_HOST_PORT, EXPECTED_PORT);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_USER_AGENT, EXPECTED_USER_AGENT);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_SCHEME, EXPECTED_SCHEME);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_ROUTE, LISTENER_PATH);
  }

  @Test
  @Description("The request span customizer informs the distributed trace context manager the correct attributes/name")
  public void requestSpan() throws Exception {
    testRequestSpanWithUri("https://www.expectedhost.com:8080/bla", EXPECTED_PORT);
  }

  @Test
  @Description("The request span customizer informs the distributed trace context manager the correct attributes/name with default port for HTTPS")
  public void requestSpanWithoutPortHTTPS() throws Exception {
    testRequestSpanWithUri("https://www.expectedhost.com/bla", "443");
  }

  @Test
  @Description("The request span customizer informs the distributed trace context manager the correct attributes/name with default port for HTTPS")
  public void requestSpanWithoutPortHTTP() throws Exception {
    testRequestSpanWithUri("http://www.expectedhost.com/bla", "80");
  }

  private void testRequestSpanWithUri(String uriString, String expectedPortValue) throws URISyntaxException {
    HttpRequest httpRequest = mock(HttpRequest.class);
    DistributedTraceContextManager distributedTraceContextManager = mock(DistributedTraceContextManager.class);
    URI uri = new URI(uriString);
    when(httpRequest.getMethod()).thenReturn("GET");
    when(httpRequest.getUri()).thenReturn(uri);
    when(httpRequest.getProtocol()).thenReturn(HTTP_1_1);

    HttpCurrentSpanCustomizer httpCurrentSpanCustomizer = getHttpRequesterCurrentSpanCustomizer(httpRequest);
    httpCurrentSpanCustomizer.customizeSpan(distributedTraceContextManager);

    verify(distributedTraceContextManager).setCurrentSpanName(EXPECTED_METHOD);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_METHOD, "GET");
    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_FLAVOR, EXPECTED_PROTOCOL_VERSION);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(HTTP_URL, uri.toString());
    verify(distributedTraceContextManager).addCurrentSpanAttribute(NET_PEER_PORT, expectedPortValue);
    verify(distributedTraceContextManager).addCurrentSpanAttribute(NET_PEER_NAME, EXPECTED_PEER_NAME);
  }

}
