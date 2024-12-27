/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.profiling.tracing;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;

/**
 * A customizer for the current span for HTTP
 *
 * @since 1.8.0
 */
public abstract class HttpCurrentSpanCustomizer {

  private static final Logger LOGGER = getLogger(HttpCurrentSpanCustomizer.class);

  public static final String HTTP_METHOD = "http.method";
  public static final String HTTP_FLAVOR = "http.flavor";
  public static final String SPAN_KIND = "span.kind.override";

  /**
   * Customize the current span using the {@link DistributedTraceContextManager}
   *
   * @param distributedTraceContextManager the {@link DistributedTraceContextManager} used for adding attributes and setting the name.
   */
  public void customizeSpan(DistributedTraceContextManager distributedTraceContextManager) {
    try {
      distributedTraceContextManager.setCurrentSpanName(getSpanName());

      distributedTraceContextManager.addCurrentSpanAttribute(HTTP_METHOD, getMethod());
      distributedTraceContextManager.addCurrentSpanAttribute(SPAN_KIND, getSpanKind());

      String flavor = getFlavor();
      if (flavor != null) {
        distributedTraceContextManager.addCurrentSpanAttribute(HTTP_FLAVOR, flavor);
      }
    } catch (Throwable e) {
      LOGGER.warn("Error on customizing Request Span.", e);
    }
  }

  protected String getSpanName() {
    return getMethod();
  }

  /**
   * @return whether this is HTTP or HTTPS.
   */
  public abstract String getProtocol();

  /**
   * @return the method for the http span.
   */
  public abstract String getMethod();

  /**
   * @return the http flavor.
   */
  public abstract String getFlavor();

  /**
   * @return the {@link URI}
   */
  public abstract URI getURI() throws URISyntaxException;

  /**
   * @return the span kind
   */
  protected abstract String getSpanKind();
}
