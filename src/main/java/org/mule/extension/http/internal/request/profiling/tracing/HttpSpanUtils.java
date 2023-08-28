/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.profiling.tracing;

import static java.lang.String.valueOf;

import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;

import org.slf4j.Logger;

/**
 * Utils for Http span creation.
 *
 * @since 1.8.0
 */
public class HttpSpanUtils {

  public static final String HTTP_STATUS_CODE = "http.status_code";
  public static final String SPAN_STATUS = "status.override";
  public static final String ERROR_STATUS = "ERROR";
  public static final String UNSET_STATUS = "UNSET";

  private HttpSpanUtils() {}

  /**
   * Adds the status code attribute.
   *
   * @param distributedTraceContextManager the {@link DistributedTraceContextManager} to use.
   * @param statusCode the status code.
   * @param logger the LOGGER to use.
   */
  public static void addStatusCodeAttribute(DistributedTraceContextManager distributedTraceContextManager,
                                            int statusCode,
                                            Logger logger) {
    try {
      if (distributedTraceContextManager != null) {
        distributedTraceContextManager.addCurrentSpanAttribute(HTTP_STATUS_CODE, valueOf(statusCode));
      }
    } catch (Throwable e) {
      logger.warn("An exception on processing the span http status code", e);
    }
  }

  /**
   * Update the server span status through the addition of an attribute
   *
   * @param distributedTraceContextManager the {@link DistributedTraceContextManager} to use.
   * @param statusCode the status code.
   * @param logger the LOGGER to use.
   */
  public static void updateServerSpanStatus(DistributedTraceContextManager distributedTraceContextManager,
                                            int statusCode,
                                            Logger logger) {
    try {
      if (distributedTraceContextManager != null) {
        if (statusCode == 500) {
          distributedTraceContextManager.addCurrentSpanAttribute(SPAN_STATUS, ERROR_STATUS);
        } else {
          distributedTraceContextManager.addCurrentSpanAttribute(SPAN_STATUS, UNSET_STATUS);
        }
      }
    } catch (Throwable e) {
      logger.warn("An exception on updating the server span status", e);
    }
  }

  /**
   * Update the client span status through the addition of an attribute
   *
   * @param distributedTraceContextManager the {@link DistributedTraceContextManager} to use.
   * @param statusCode the status code.
   * @param logger the LOGGER to use.
   */
  public static void updateClientSpanStatus(DistributedTraceContextManager distributedTraceContextManager,
                                            int statusCode,
                                            Logger logger) {
    try {
      if (distributedTraceContextManager != null) {
        if (statusCode >= 400) {
          distributedTraceContextManager.addCurrentSpanAttribute(SPAN_STATUS, "ERROR");
        } else {
          distributedTraceContextManager.addCurrentSpanAttribute(SPAN_STATUS, "UNSET");
        }
      }
    } catch (Throwable e) {
      logger.warn("An exception on updating the client span status", e);
    }
  }

}
