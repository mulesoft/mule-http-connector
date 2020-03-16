/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.util.Collections.unmodifiableSet;
import static org.mule.extension.http.api.notification.HttpNotificationAction.REQUEST_COMPLETE;
import static org.mule.extension.http.api.notification.HttpNotificationAction.REQUEST_START;

import org.mule.runtime.extension.api.annotation.notification.NotificationActionProvider;
import org.mule.runtime.extension.api.notification.NotificationActionDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * Set of {@link NotificationActionDefinition}s emitted by the HTTP request operation.
 *
 * @since 1.1
 */
public class RequestNotificationActionProvider implements NotificationActionProvider {

  @Override
  public Set<NotificationActionDefinition> getNotificationActions() {
    final Set<NotificationActionDefinition> notificationActions = new HashSet<>();

    notificationActions.add(REQUEST_START);
    notificationActions.add(REQUEST_COMPLETE);

    return unmodifiableSet(notificationActions);
  }

}
