/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpConstants.Method.POST;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.api.notification.HttpRequestNotificationData;
import org.mule.extension.http.api.notification.HttpResponseNotificationData;
import org.mule.runtime.api.notification.ExtensionNotification;
import org.mule.runtime.api.notification.ExtensionNotificationListener;
import org.mule.runtime.api.notification.NotificationListenerRegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.junit.Test;

public class HttpRequestNotificationsTestCase extends AbstractHttpRequestTestCase {

  @Inject
  private NotificationListenerRegistry notificationListenerRegistry;

  @Override
  protected String getConfigFile() {
    return "http-request-notifications-config.xml";
  }

  @Test
  public void receiveNotification() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);

    TestExtensionNotificationListener listener = new TestExtensionNotificationListener(latch);
    notificationListenerRegistry.registerListener(listener);

    flowRunner("requestFlow").withPayload(TEST_MESSAGE).run().getMessage();

    latch.await(1000, MILLISECONDS);

    assertThat(listener.getNotifications().stream().map(n -> n.getAction().getIdentifier()).collect(toList()),
               containsInAnyOrder("REQUEST_START", "REQUEST_COMPLETE"));

    // verify that request data was collected
    ExtensionNotification extensionNotification1 = listener.getNotifications().get(0);
    assertThat(extensionNotification1.getData().getValue(), is(instanceOf(HttpRequestNotificationData.class)));
    HttpRequestNotificationData requestData = (HttpRequestNotificationData) extensionNotification1.getData().getValue();
    assertThat(requestData.getMethod(), is(POST.name()));
    assertThat(requestData.getQueryParams().getAll("query"), containsInAnyOrder("param", "otherParam"));
    assertThat(requestData.getHeaders().getAll("header"), containsInAnyOrder("value", "otherValue"));

    // verify that response data was collected
    ExtensionNotification extensionNotification2 = listener.getNotifications().get(1);
    assertThat(extensionNotification2.getData().getValue(), is(instanceOf(HttpResponseNotificationData.class)));
    HttpResponseNotificationData responseData = (HttpResponseNotificationData) extensionNotification2.getData().getValue();
    assertThat(responseData.getStatusCode(), is(OK.getStatusCode()));
    assertThat(responseData.getReasonPhrase(), is(OK.getReasonPhrase()));
  }

  private class TestExtensionNotificationListener implements ExtensionNotificationListener {

    private CountDownLatch latch;
    private List<ExtensionNotification> notifications = new LinkedList<>();

    public TestExtensionNotificationListener(CountDownLatch latch) {
      this.latch = latch;
    }

    @Override
    public void onNotification(ExtensionNotification notification) {
      notifications.add(notification);
      latch.countDown();
    }

    public List<ExtensionNotification> getNotifications() {
      return notifications;
    }
  }

}
