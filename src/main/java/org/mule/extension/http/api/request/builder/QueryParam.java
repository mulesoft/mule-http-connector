/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * Represents an HTTP Query Parameter
 * 
 * @since 1.1
 */
public class QueryParam {

  @Parameter
  private String key;

  @Parameter
  private String value;

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

}
