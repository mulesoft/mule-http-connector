/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.http.internal.listener.profiling.tracing;

import static java.lang.String.valueOf;

import static com.google.common.net.HttpHeaders.USER_AGENT;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.internal.request.profiling.tracing.HttpCurrentSpanCustomizer;
import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;

/**
 * A customizer for the current span for HTTP Requests
 *
 * @since 1.8.0
 */
public class HttpListenerCurrentSpanCustomizer extends HttpCurrentSpanCustomizer {

  private static final Logger LOGGER = getLogger(HttpListenerCurrentSpanCustomizer.class);

  public static final String HTTP_TARGET = "http.target";
  public static final String NET_HOST_NAME = "net.host.name";
  public static final String NET_HOST_PORT = "net.host.port";
  public static final String HTTP_USER_AGENT = "http.user_agent";
  private final HttpRequestAttributes attributes;
  private final String host;

  public HttpListenerCurrentSpanCustomizer(HttpRequestAttributes attributes, String host) {
    this.attributes = attributes;
    this.host = host;
  }

  public static HttpCurrentSpanCustomizer getHttpListenerCurrentSpanCustomizer(HttpRequestAttributes attributes,
                                                                               String host) {
    return new HttpListenerCurrentSpanCustomizer(attributes, host);
  }

  @Override
  public void customizeSpan(DistributedTraceContextManager distributedTraceContextManager) {
    super.customizeSpan(distributedTraceContextManager);

    try {
      distributedTraceContextManager.addCurrentSpanAttribute(HTTP_TARGET, attributes.getListenerPath());
      distributedTraceContextManager.addCurrentSpanAttribute(NET_HOST_NAME, host);
      distributedTraceContextManager.addCurrentSpanAttribute(NET_HOST_PORT, valueOf(getURI().getPort()));
      String userAgent = attributes.getHeaders().get(USER_AGENT);

      if (userAgent != null) {
        distributedTraceContextManager.addCurrentSpanAttribute(HTTP_USER_AGENT, userAgent);
      }

    } catch (Throwable e) {
      LOGGER.warn("Error on setting listener span attributes.", e);
    }
  }

  @Override
  public String getProtocol() {
    return attributes.getScheme().toUpperCase();
  }

  @Override
  public String getMethod() {
    return attributes.getMethod().toUpperCase();
  }

  @Override
  public String getFlavor() {
    return resolveFlavor(attributes.getVersion());
  }

  private String resolveFlavor(String version) {
    if (version == null) {
      return null;
    }

    return version.substring(version.indexOf("/") + 1);
  }

  @Override
  public URI getURI() throws URISyntaxException {
    return new URI(attributes.getScheme() + ":/" + attributes.getLocalAddress() + attributes.getListenerPath());
  }

  @Override
  protected String getSpanName() {
    return attributes.getListenerPath();
  }
}
