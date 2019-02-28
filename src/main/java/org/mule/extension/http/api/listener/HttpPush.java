/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;

@Alias("push")
public class HttpPush {

  @Parameter
  @Expression(NOT_SUPPORTED)
  private String resource;

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getResource() {
    return resource;
  }

}
