/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.runtime.extension.api.annotation.param.display.Placement.DEFAULT_TAB;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@ExclusiveOptionals(isOneRequired = true)
public class UriSettings {

  /**
   * Path where the request will be sent.
   */
  @Parameter
  @Optional
  @Placement(tab = DEFAULT_TAB, order = 2)
  private String path = "/";

  /**
   * URL where to send the request.
   */
  @Parameter
  @Optional
  @DisplayName("URL")
  @Example("http://www.mulesoft.com")
  @Placement(tab = DEFAULT_TAB, order = 1)
  private String url;

  public String getPath() {
    return path;
  }

  public String getUrl() {
    return url;
  }
}
