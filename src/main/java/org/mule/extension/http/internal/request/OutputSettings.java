/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSE;

import org.mule.extension.http.internal.HttpMetadataKey;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

/**
 * Groups parameters which control how the operation output is generated
 *
 * @since 1.0
 */
public final class OutputSettings {

  @Parameter
  @Optional(defaultValue = "STREAM")
  @MetadataKeyId
  @Placement(tab = RESPONSE)
  private HttpMetadataKey outputType;

  public HttpMetadataKey getOutputType() {
    return outputType;
  }
}
