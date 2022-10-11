/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

/**
 * Utils for creating http spans both in listeners and requests.
 *
 * @since 1.8.0
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
      logger.warn("An exception on processing the listener span http status code", e);
    }
  }

}
