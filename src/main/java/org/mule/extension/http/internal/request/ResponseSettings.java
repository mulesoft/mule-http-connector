/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.extension.http.api.request.validator.ResponseValidator;
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

  /**
   * Configures a default error handling of the response.
   */
  @Parameter
  @Optional
  private ResponseValidator responseValidator;

  public Integer getResponseTimeout() {
    return responseTimeout;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Integer responseTimeout;
    private ResponseValidator responseValidator;

    public Builder withResponseTimeout(Integer responseTimeout) {
      this.responseTimeout = responseTimeout;
      return this;
    }

    public Builder withResponseValidator(ResponseValidator responseValidator) {
      this.responseValidator = responseValidator;
      return this;
    }

    public ResponseSettings build() {
      ResponseSettings settings = new ResponseSettings();
      settings.responseTimeout = this.responseTimeout;
      settings.responseValidator = this.responseValidator;
      return settings;
    }

  }
}
