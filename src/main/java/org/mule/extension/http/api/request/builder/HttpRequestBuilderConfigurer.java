/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import org.mule.api.annotation.NoImplement;
import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

/**
 * Builder that can be transform to an {@link HttpRequestBuilder} given an {@link HttpRequesterConfig}
 *
 * @since 1.7
 */
@NoImplement
public interface HttpRequestBuilderConfigurer {

  /**
   * @param config
   * @return the equivalent HttpRequestBuilder with all the information needed to create the request
   */
  HttpRequestBuilder toHttpRequestBuilder(HttpRequesterConfig config);

  /**
   * @return the body of this request builder, as {@link TypedValue}
   */
  TypedValue<?> getBodyAsTypedValue();
}
