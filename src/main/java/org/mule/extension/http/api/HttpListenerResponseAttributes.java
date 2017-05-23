/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import org.mule.runtime.http.api.domain.ParameterMap;

/**
 * HTTP listener specific {@link HttpResponseAttributes}. Only this kind of attributes will be considered within an error message.
 *
 * @since 1.0
 */
public class HttpListenerResponseAttributes extends HttpResponseAttributes {

  public HttpListenerResponseAttributes(int statusCode, String reasonPhrase, ParameterMap headers) {
    super(statusCode, reasonPhrase, headers);
  }

}
