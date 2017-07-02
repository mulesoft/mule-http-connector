/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.internal.HttpConnectorConstants.REQUEST;
import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSE;
import org.mule.extension.http.internal.HttpStreamingType;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

/**
 * Group which holds operation parameters which overrides config settings
 *
 * @since 1.0
 */
public final class ConfigurationOverrides {

  /**
   * Specifies whether to follow redirects or not.
   */
  @Parameter
  @ConfigOverride
  @Placement(tab = REQUEST, order = 1)
  private boolean followRedirects;

  /**
   * Defines if the request should contain a body or not.
   */
  @Parameter
  @ConfigOverride
  @Placement(tab = REQUEST, order = 2)
  private HttpSendBodyMode sendBodyMode;

  /**
   * Defines if the request should be sent using streaming or not.
   */
  @Parameter
  @ConfigOverride
  @Placement(tab = REQUEST, order = 3)
  private HttpStreamingType requestStreamingMode;

  /**
   * Maximum time that the request element will block the execution of the flow waiting for the HTTP response.
   */
  @Parameter
  @ConfigOverride
  @Placement(tab = RESPONSE, order = 1)
  private Integer responseTimeout;

  public boolean getFollowRedirects() {
    return followRedirects;
  }

  public HttpSendBodyMode getSendBodyMode() {
    return sendBodyMode;
  }

  public HttpStreamingType getRequestStreamingMode() {
    return requestStreamingMode;
  }

  public Integer getResponseTimeout() {
    return responseTimeout;
  }
}
