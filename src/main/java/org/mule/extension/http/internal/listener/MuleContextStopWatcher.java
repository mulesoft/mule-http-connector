/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static org.mule.runtime.core.api.context.notification.MuleContextNotification.CONTEXT_STARTING;
import static org.mule.runtime.core.api.context.notification.MuleContextNotification.CONTEXT_STOPPING;
import org.mule.runtime.core.api.context.notification.MuleContextNotification;
import org.mule.runtime.core.api.context.notification.MuleContextNotificationListener;

class MuleContextStopWatcher implements MuleContextNotificationListener<MuleContextNotification> {

  private volatile boolean stopping = false;

  @Override
  public synchronized void onNotification(MuleContextNotification notification) {
    if (notification.getAction().getActionId() == CONTEXT_STOPPING) {
      stopping = true;
    } else if (notification.getAction().getActionId() == CONTEXT_STARTING) {
      stopping = false;
    }
  }

  synchronized boolean isStopping() {
    return stopping;
  }
}
