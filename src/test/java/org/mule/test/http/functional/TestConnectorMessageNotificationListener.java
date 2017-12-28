/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.apache.commons.collections.CollectionUtils.collect;
import static org.apache.commons.collections.CollectionUtils.select;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.mule.runtime.api.notification.ConnectorMessageNotification;
import org.mule.runtime.api.notification.ConnectorMessageNotificationListener;
import org.mule.runtime.api.notification.Notification;
import org.mule.runtime.api.notification.NotificationListener;
import org.mule.runtime.core.api.context.notification.ServerNotificationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.collections.Transformer;

public class TestConnectorMessageNotificationListener
    implements ConnectorMessageNotificationListener<ConnectorMessageNotification> {

  private final CountDownLatch latch;
  private final String expectedExchangePoint;

  private List<ConnectorMessageNotification> notifications = new ArrayList<>();

  public TestConnectorMessageNotificationListener() {
    // Dummy listener for registration
    latch = null;
    expectedExchangePoint = null;
  }

  public TestConnectorMessageNotificationListener(CountDownLatch latch, String expectedExchangePoint) {
    this.latch = latch;
    this.expectedExchangePoint = expectedExchangePoint;
  }

  @Override
  public boolean isBlocking() {
    return false;
  }

  @Override
  public void onNotification(ConnectorMessageNotification notification) {
    notifications.add(notification);
    if (latch != null) {
      assertThat(notification.getLocationUri(), is(expectedExchangePoint));
      latch.countDown();
    }
  }

  public List<String> getNotificationActionNames() {
    return (List<String>) collect(notifications, (Transformer) input -> ((ConnectorMessageNotification) input).getActionName());
  }

  /**
   * Gets the list of notifications for the action name.
   * 
   * @param actionName
   * @return The notifications sent for the given action.
   */
  public List<ConnectorMessageNotification> getNotifications(final String actionName) {
    return (List<ConnectorMessageNotification>) select(notifications, object -> ((ConnectorMessageNotification) object)
        .getActionName().equals(actionName));
  }

  public static ServerNotificationManager register(ServerNotificationManager serverNotificationManager) {
    final Map<Class<? extends NotificationListener>, Set<Class<? extends Notification>>> mapping =
        serverNotificationManager.getInterfaceToTypes();
    if (!mapping.containsKey(ConnectorMessageNotificationListener.class)) {
      serverNotificationManager.addInterfaceToType(TestConnectorMessageNotificationListener.class,
                                                   ConnectorMessageNotification.class);
      serverNotificationManager.addListener(new TestConnectorMessageNotificationListener());
    }
    return serverNotificationManager;
  }


}
