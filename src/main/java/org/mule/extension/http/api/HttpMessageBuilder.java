/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;

/**
 * Base component to create HTTP messages.
 *
 * @since 1.0
 */
public abstract class HttpMessageBuilder {

  public abstract MultiMap<String, String> getHeaders();

  public abstract void setHeaders(MultiMap<String, String> headers);

  public abstract TypedValue<Object> getBody();

  public abstract void setBody(TypedValue<Object> body);
}
