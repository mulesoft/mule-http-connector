/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.notification;

import org.mule.runtime.extension.api.notification.NotificationActionDefinition;

/**
 * HTTP notification actions.
 *
 * @since 1.1
 */
public enum HttpNotificationAction implements NotificationActionDefinition<HttpNotificationAction> {

  REQUEST_RECEIVED(HttpRequestData.class), REQUEST_START(HttpRequestData.class), REQUEST_COMPLETE(HttpResponseData.class);

  private final Class<?> dataType;

  HttpNotificationAction(Class<?> dataType) {
    this.dataType = dataType;
  }

  @Override
  public Class<?> getDataType() {
    return dataType;
  }

}
