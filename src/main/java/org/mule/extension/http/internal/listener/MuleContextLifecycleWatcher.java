/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static org.mule.runtime.core.api.context.notification.MuleContextNotification.CONTEXT_STARTING;
import static org.mule.runtime.core.api.context.notification.MuleContextNotification.CONTEXT_STOPPING;
import org.mule.runtime.core.api.context.notification.MuleContextNotification;
import org.mule.runtime.core.api.context.notification.MuleContextNotificationListener;

public class MuleContextLifecycleWatcher implements MuleContextNotificationListener<MuleContextNotification> {

  private volatile boolean isStarted = true;

  @Override
  public synchronized void onNotification(MuleContextNotification notification) {
    if (notification.getAction().getActionId() == CONTEXT_STOPPING) {
      isStarted = false;
    } else if (notification.getAction().getActionId() == CONTEXT_STARTING) {
      isStarted = true;
    }
  }

  public synchronized boolean isMuleContextStarted() {
    return isStarted;
  }
}
