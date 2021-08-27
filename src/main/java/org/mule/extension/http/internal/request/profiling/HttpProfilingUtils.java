/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.profiling;

import static java.lang.System.currentTimeMillis;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.profiling.type.context.ExtensionProfilingEventContext;
import org.mule.runtime.extension.api.runtime.operation.Result;

/**
 * A helper class for profiling HTTP.
 */
public class HttpProfilingUtils {

  /**
   * Creates an {@link ExtensionProfilingEventContext} for a HTTP Request.
   *
   * @param result the {@link Result} corresponding to a HTTP Request
   * @param correlationId the correlation id associated to the event involved in the HTTP request
   * @return the {@link ExtensionProfilingEventContext} with the profiling data.
   */
  public static ExtensionProfilingEventContext getHttpRequestResponseProfilingEventContext(Result<Object, HttpResponseAttributes> result,
                                                                                           String correlationId) {
    return new HttpRequestProfilingEventContext(result, correlationId, currentTimeMillis());
  }
}
