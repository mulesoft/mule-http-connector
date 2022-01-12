/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.extension.http.api.request.builder.CorrelationData;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.util.Optional;

public interface RequestCreator {

  HttpRequestBuilder createRequestBuilder(HttpRequesterConfig config);

  TypedValue<?> getBody();

  Optional<CorrelationData> getCorrelationData();
}
