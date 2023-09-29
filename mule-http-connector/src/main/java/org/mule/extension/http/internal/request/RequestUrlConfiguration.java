/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

public final class RequestUrlConfiguration {

  /**
   * Base path to use for all requests that reference this config.
   */
  @Parameter
  @Optional(defaultValue = "/")
  private String basePath;

  public String getBasePath() {
    return basePath;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String basePath;

    public Builder withBasePath(String basePath) {
      this.basePath = basePath;
      return this;
    }

    public RequestUrlConfiguration build() {
      RequestUrlConfiguration config = new RequestUrlConfiguration();
      config.basePath = this.basePath;
      return config;
    }

  }
}
