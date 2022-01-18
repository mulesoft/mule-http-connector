/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.util.Optional;

/**
 * Creates everything the {@link HttpRequestFactory#create} needs to create and modify the {@link HttpRequestBuilder}
 *
 * @since 1.7
 */
public interface RequestCreator {

  /**
   * @param config
   * @return the {@link HttpRequestBuilder} to configure to create the HTTP Request
   */
  HttpRequestBuilder createRequestBuilder(HttpRequesterConfig config);

  /**
   * @return the Body to turn into an {@link HttpEntity}
   */
  TypedValue<?> getBody();

  /**
   * @return if any, the {@link CorrelationData} for this HTTP Request
   */
  Optional<CorrelationData> getCorrelationData();
}
