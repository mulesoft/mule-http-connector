/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.profiling;

import static org.mule.runtime.api.profiling.type.RuntimeProfilingEventTypes.EXTENSION_PROFILING_EVENT;

import org.mule.extension.http.api.request.HttpRequestOperations;
import org.mule.runtime.api.profiling.ProfilingService;

import javax.inject.Inject;

/**
 * Adaptor for {@link ProfilingService} to obtain a {@link HttpRequestResponseProfilingDataProducerAdaptor} for generating profiling related HTTP requests.
 * This is implemented so that no need to change the min mule version is needed in the HTTP connector.
 *
 * @see HttpRequestOperations#initialise()
 */
public class HttpProfilingServiceAdaptor {

  @Inject
  private ProfilingService profilingService;

  /**
   * @return the adaptor for the {@link org.mule.runtime.api.profiling.ProfilingDataProducer}
   */
  public HttpRequestResponseProfilingDataProducerAdaptor getProfilingHttpRequestDataProducer() {
    return new HttpRequestResponseProfilingDataProducerAdaptor((profilingService
        .getProfilingDataProducer(EXTENSION_PROFILING_EVENT)));
  }
}
