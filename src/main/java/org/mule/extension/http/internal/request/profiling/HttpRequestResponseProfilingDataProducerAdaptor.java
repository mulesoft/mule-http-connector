/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.profiling;

import static org.mule.extension.http.internal.request.profiling.HttpProfilingUtils.getHttpRequestResponseProfilingEventContext;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.internal.request.HttpRequestOperations;
import org.mule.runtime.api.profiling.ProfilingDataProducer;
import org.mule.runtime.api.profiling.type.context.ExtensionProfilingEventContext;
import org.mule.runtime.extension.api.runtime.operation.Result;

/**
 * An adaptor for {@link ProfilingDataProducer} to produce data related to HTTP requests.
 * This is implemented so that no need to change the min mule version is needed in the HTTP connector.
 * In some environments {@link ProfilingDataProducer} class is not available.
 * This class will only be loaded if it is available according to the mule runtime version.
 *
 * @see HttpRequestOperations#initialise()
 */
public class HttpRequestResponseProfilingDataProducerAdaptor {

  ProfilingDataProducer<ExtensionProfilingEventContext> profilingDataProducer;

  public HttpRequestResponseProfilingDataProducerAdaptor(ProfilingDataProducer<ExtensionProfilingEventContext> profilingDataProducer) {
    this.profilingDataProducer = profilingDataProducer;
  }

  /**
   * Triggers a profiling event indicating that an HTTP Request Response was received
   * @param result the result to construct the {@link org.mule.runtime.api.profiling.ProfilingEventContext}.
   */
  public void triggerProfilingEvent(Result<Object, HttpResponseAttributes> result, String correlationId) {
    profilingDataProducer.triggerProfilingEvent(getHttpRequestResponseProfilingEventContext(result, correlationId));
  }
}
