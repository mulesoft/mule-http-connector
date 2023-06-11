/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.runtime.api.meta.ExpressionSupport.SUPPORTED;

import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * Groups parameters which configure how a request is traced
 *
 * @since 1.0
 */
public final class TracingSettings {

  /**
   * Indicates what headers should not be exported as attributes when generating Open Telemetry traces. By default, common headers associated with credentials are skipped.
   *
   * @since 1.8.0
   */
  @Parameter
  @Optional(defaultValue = "client_id, client_secret, Authorization")
  @Expression(SUPPORTED)
  private String skipHeadersOnTracing;

  public String getSkipHeadersOnTracing() {
    return skipHeadersOnTracing;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String skipHeadersOnTracing;

    public Builder withFollowRedirects(String skipHeadersOnTracing) {
      this.skipHeadersOnTracing = skipHeadersOnTracing;
      return this;
    }

    public TracingSettings build() {
      TracingSettings settings = new TracingSettings();
      settings.skipHeadersOnTracing = this.skipHeadersOnTracing;
      return settings;
    }
  }
}
