/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * Groups parameters regarding how to generate responses
 *
 * @since 1.0
 */
public final class ResponseSettings {

  /**
   * Maximum time that the request element will block the execution of the flow waiting for the HTTP response. If this value is
   * not present, the default response timeout from the Mule configuration will be used.
   */
  @Parameter
  @Optional
  private Integer responseTimeout;

  public Integer getResponseTimeout() {
    return responseTimeout;
  }
}
