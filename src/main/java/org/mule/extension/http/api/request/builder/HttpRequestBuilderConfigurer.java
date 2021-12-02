/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

/**
 * ADD JAVA DOC HERE
 *
 * @since 1.7
 */
public interface HttpRequestBuilderConfigurer {

  HttpRequestBuilder toHttpRequestBuilder(HttpRequesterConfig config);

  TypedValue<?> getBodyAsTypedValue();
}
