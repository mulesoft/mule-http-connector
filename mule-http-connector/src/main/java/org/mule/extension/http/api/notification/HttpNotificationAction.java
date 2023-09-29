/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.notification;

import static org.mule.runtime.api.metadata.DataType.fromType;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.extension.api.notification.NotificationActionDefinition;

/**
 * HTTP notification actions.
 *
 * @since 1.1
 */
public enum HttpNotificationAction implements NotificationActionDefinition<HttpNotificationAction> {

  /**
   * An outgoing HTTP request has been sent.
   */
  REQUEST_START(fromType(HttpRequestNotificationData.class)),
  /**
   * An incoming HTTP response has been received and the triggering request is complete.
   */
  REQUEST_COMPLETE(fromType(HttpResponseNotificationData.class));

  private final DataType dataType;

  HttpNotificationAction(DataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public DataType getDataType() {
    return dataType;
  }

}
