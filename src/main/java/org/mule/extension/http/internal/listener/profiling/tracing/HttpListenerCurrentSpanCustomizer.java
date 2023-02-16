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
import org.mule.runtime.api.util.MultiMap;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Arrays;

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
  public static final String HTTP_SCHEME = "http.scheme";
  private static final String SPAN_KIND_NAME = "SERVER";

  private final HttpRequestAttributes attributes;
  private final String host;
  private final int port;

  //W-12558102
  private final List<String> skipAttributesList;

  private HttpListenerCurrentSpanCustomizer(HttpRequestAttributes attributes, String host, int port, String skipAttributes) {
    this.attributes = attributes;
    this.host = host;
    this.port = port;
    //W-12558102
    this.skipAttributesList = Arrays.asList(skipAttributes.split(",", -1));
    this.skipAttributesList.replaceAll(String::trim);
  }

  public static HttpCurrentSpanCustomizer getHttpListenerCurrentSpanCustomizer(HttpRequestAttributes attributes,
                                                                               String host,
                                                                               int port,
                                                                               //W-... gp
                                                                               String skipAttributes) {
    return new HttpListenerCurrentSpanCustomizer(attributes, host, port, skipAttributes);
  }

  @Override
  public void customizeSpan(DistributedTraceContextManager distributedTraceContextManager) {
    super.customizeSpan(distributedTraceContextManager);

    try {
      distributedTraceContextManager.addCurrentSpanAttribute(HTTP_TARGET, attributes.getListenerPath());
      distributedTraceContextManager.addCurrentSpanAttribute(NET_HOST_NAME, host);
      distributedTraceContextManager.addCurrentSpanAttribute(NET_HOST_PORT, valueOf(getURI().getPort()));
      distributedTraceContextManager.addCurrentSpanAttribute(HTTP_SCHEME, attributes.getScheme());
      //W-12558102 - Parsing HTTP headers as Span Attributes
      MultiMap<String, String> headers = getHeaders();
      headers.keySet().forEach(key -> distributedTraceContextManager.addCurrentSpanAttribute(key, headers.get(key)));

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
    return new URI(attributes.getScheme(), null, host, port, attributes.getListenerPath(), null, null);
  }

  @Override
  protected String getSpanKind() {
    return SPAN_KIND_NAME;
  }

  @Override
  protected String getSpanName() {
    return attributes.getListenerPath();
  }

  /**
   * W-12558102
   * This provides a transparent way to obtain the definitive list of headers to add in the spans associated with Otel tracing.
   * This list will have all headers except those that have been skipped via the skipHeadersOnTracing property.
   */
  @Override
  public MultiMap<String, String> getHeaders() {
    MultiMap<String, String> modifiedHeaders = new MultiMap<String, String>();
    attributes.getHeaders().keySet().forEach(key -> {
      if (!skipAttributesList.contains(key)) {
        modifiedHeaders.put(key, attributes.getHeaders().get(key));
      }
    });
    return modifiedHeaders;
  }
}
