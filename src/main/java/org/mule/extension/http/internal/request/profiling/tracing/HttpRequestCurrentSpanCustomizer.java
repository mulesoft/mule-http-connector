/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.http.internal.request.profiling.tracing;

import static org.mule.runtime.http.api.domain.HttpProtocol.HTTP_0_9;
import static org.mule.runtime.http.api.domain.HttpProtocol.HTTP_1_0;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.internal.listener.profiling.tracing.HttpListenerCurrentSpanCustomizer;
import org.mule.runtime.http.api.domain.HttpProtocol;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;
import org.mule.runtime.api.util.MultiMap;

import java.net.URI;
import java.util.List;
import org.slf4j.Logger;

/**
 * A customizer for the current span for HTTP Requests
 *
 * @since 1.8.0
 */
public class HttpRequestCurrentSpanCustomizer extends HttpCurrentSpanCustomizer {

  private static final Logger LOGGER = getLogger(HttpRequestCurrentSpanCustomizer.class);

  public static final String HTTP_URL = "http.url";
  public static final String NET_PEER_PORT = "net.peer.port";
  public static final String NET_PEER_NAME = "net.peer.name";
  private static final String SPAN_KIND_NAME = "CLIENT";

  public static final String PROTOCOL_VERSION_0_9 = "0.9";
  public static final String PROTOCOL_VERSION_1_0 = "1.0";
  public static final String PROTOCOL_VERSION_1_1 = "1.1";

  //W-12558102
  private final List<String> skipAttributesList;

  /**
   * @return a new instance of a {@link HttpRequestCurrentSpanCustomizer}.
   */
  public static HttpCurrentSpanCustomizer getHttpRequesterCurrentSpanCustomizer(HttpRequest httpRequest, String skipAttributes) {
    return new HttpRequestCurrentSpanCustomizer(httpRequest, skipAttributes);
  }

  private HttpRequest httpRequest;

  private HttpRequestCurrentSpanCustomizer(HttpRequest httpRequest, String skipAttributes) {
    this.httpRequest = httpRequest;
    this.skipAttributesList = asList(skipAttributes.split(",", -1));
    this.skipAttributesList.replaceAll(String::trim);
  }

  @Override
  public void customizeSpan(DistributedTraceContextManager distributedTraceContextManager) {
    super.customizeSpan(distributedTraceContextManager);

    try {
      distributedTraceContextManager.addCurrentSpanAttribute(HTTP_URL, getURI().toString());
      if (getURI().getPort() == -1) {
        distributedTraceContextManager.addCurrentSpanAttribute(NET_PEER_PORT, valueOf(getURI().toURL().getDefaultPort()));
      } else {
        distributedTraceContextManager.addCurrentSpanAttribute(NET_PEER_PORT, valueOf(getURI().getPort()));
      }
      distributedTraceContextManager.addCurrentSpanAttribute(NET_PEER_NAME, getURI().getHost());
      MultiMap<String, String> headers = getHeaders(httpRequest.getHeaders(), skipAttributesList);
      headers.keySet().forEach(key -> distributedTraceContextManager.addCurrentSpanAttribute(key, headers.get(key)));

    } catch (Throwable e) {
      LOGGER.warn("Error on setting the request span attributes", e);
    }
  }

  @Override
  public String getProtocol() {
    return httpRequest.getUri().getScheme().toUpperCase();
  }

  @Override
  public String getMethod() {
    return httpRequest.getMethod().toUpperCase();
  }

  @Override
  public String getFlavor() {
    return getHttpProtocolVersionFrom(httpRequest.getProtocol());
  }

  @Override
  public URI getURI() {
    return httpRequest.getUri();
  }

  @Override
  protected String getSpanKind() {
    return SPAN_KIND_NAME;
  }

  private static String getHttpProtocolVersionFrom(HttpProtocol protocol) {
    if (protocol == null) {
      return null;
    }

    if (protocol.equals(HTTP_0_9)) {
      return PROTOCOL_VERSION_0_9;
    }

    if (protocol.equals(HTTP_1_0)) {
      return PROTOCOL_VERSION_1_0;
    }

    return PROTOCOL_VERSION_1_1;
  }

}
