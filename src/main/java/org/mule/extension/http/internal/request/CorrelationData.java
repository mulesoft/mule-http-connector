/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.OutboundCorrelationStrategy;

/**
 * Models all the data needed to build an HTTP Request with Correlation ID
 *
 * @since 1.7
 */
public final class CorrelationData {

  private CorrelationInfo correlationInfo;
  private OutboundCorrelationStrategy sendCorrelationId;
  private String correlationId;

  public CorrelationData(CorrelationInfo correlationInfo, OutboundCorrelationStrategy sendCorrelationId, String correlationId) {
    this.correlationInfo = correlationInfo;
    this.sendCorrelationId = sendCorrelationId;
    this.correlationId = correlationId;
  }

  public CorrelationInfo getCorrelationInfo() {
    return correlationInfo;
  }

  public OutboundCorrelationStrategy getSendCorrelationId() {
    return sendCorrelationId;
  }

  public String getCorrelationId() {
    return correlationId;
  }
}
