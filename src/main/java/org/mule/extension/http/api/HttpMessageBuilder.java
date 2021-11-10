/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import org.mule.api.annotation.NoExtend;
import org.mule.extension.http.api.request.HttpRequester;
import org.mule.extension.http.api.request.HttpResponseToResult;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.server.HttpServerProperties;

/**
 * Base component to create HTTP messages.
 *
 * @since 1.0
 */
@NoExtend
public abstract class HttpMessageBuilder {

  public abstract MultiMap<String, String> getHeaders();

  public abstract void setHeaders(MultiMap<String, String> headers);

  public abstract TypedValue<Object> getBody();

  public abstract void setBody(TypedValue<Object> body);

  /**
   * Forces a re-read of any system properties that affect HTTP messages building logic.
   */
  public static void refreshSystemProperties() {
    HttpRequester.refreshSystemProperties();
    HttpResponseToResult.refreshSystemProperties();
    HttpServerProperties.refreshSystemProperties();
    HttpRequesterRequestBuilder.refreshSystemProperties();
  }
}
