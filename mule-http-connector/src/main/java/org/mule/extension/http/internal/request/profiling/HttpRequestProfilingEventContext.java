/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.profiling;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.profiling.type.context.ExtensionProfilingEventContext;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A {@link ExtensionProfilingEventContext} that corresponds to an HTTP Response Profiling Event.
 */
public class HttpRequestProfilingEventContext implements ExtensionProfilingEventContext {

  /**
   * The Profiling Component Event Identifier for the Http Connector
   */
  public static final String HTTP_CONNECTOR = "HTTP_CONNECTOR";

  /**
   * The Event Identifier for the Request Response.
   */
  public static final String HTTP_REQUEST_RESPONSE = "HTTP_REQUEST_RESPONSE";

  /**
   * The key for the HTTP request response code.
   */
  public static final String STATUS_CODE = "STATUS_CODE";

  /**
   * The key for the correlation id.
   */
  public static final String CORRELATION_ID = "CORRELATION_ID";

  /**
   * The reason phrase for the response.
   */
  public static final String REASON_PHRASE = "REASON_PHRASE";

  private Result<Object, HttpResponseAttributes> result;

  private String correlationId;

  private final long triggerTimestamp;

  private final Map<String, Supplier<Optional<Object>>> profilingDataSuppliers =
      new HashMap<String, Supplier<Optional<Object>>>() {

        {
          put(CORRELATION_ID, () -> ofNullable(correlationId));
          put(STATUS_CODE, () -> result.getAttributes().map(HttpResponseAttributes::getStatusCode));
          put(REASON_PHRASE, () -> result.getAttributes().map(HttpResponseAttributes::getReasonPhrase));
        }
      };

  public HttpRequestProfilingEventContext(Result<Object, HttpResponseAttributes> result, String correlationId,
                                          long triggerTimestamp) {
    this.triggerTimestamp = triggerTimestamp;
    this.correlationId = correlationId;
    this.result = result;
  }

  @Override
  public long getTriggerTimestamp() {
    return triggerTimestamp;
  }

  @Override
  public String getProfilingDataSourceIdentifier() {
    return HTTP_CONNECTOR;
  }

  @Override
  public String getExtensionEventSubtypeIdentifier() {
    return HTTP_REQUEST_RESPONSE;
  }

  @Override
  public Optional<Object> get(String key) {
    if (profilingDataSuppliers.containsKey(key)) {
      return profilingDataSuppliers.get(key).get();
    }

    return empty();
  }
}
