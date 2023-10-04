/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.builder;

import org.mule.extension.http.api.HttpMessageBuilder;

/**
 * Base class for a {@link HttpMessageBuilder} which returns responses
 *
 * @since 1.0
 */
public abstract class HttpListenerResponseBuilder extends HttpMessageBuilder {

  public abstract Integer getStatusCode();

  public abstract String getReasonPhrase();

  public abstract void setStatusCode(Integer statusCode);

  public abstract void setReasonPhrase(String reasonPhrase);

}
