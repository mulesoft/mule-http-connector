/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.api.config.MuleProperties.MULE_CORRELATION_ID_PROPERTY;
import static org.mule.runtime.http.api.HttpHeaders.Names.X_CORRELATION_ID;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.Collection;

import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;

public class HttpRequestCorrelationIdTestCase extends AbstractHttpRequestTestCase {

  private static final String DEFAULT_CORRELATION_ID = "defaultCorrelationId";
  private static final String CUSTOM_CORRELATION_ID = "customCorrelationId";
  private static final String LISTENER_CORRELATION_ID = "listenerCorrelationId";

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  protected String getConfigFile() {
    return "http-request-correlation-id-config.xml";
  }

  @Test
  public void sendDefaultCorrelationId() throws Exception {
    CoreEvent event = flowRunner("requestFlow")
        .withSourceCorrelationId(DEFAULT_CORRELATION_ID)
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();

    assertThat(event.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
    assertCorrelationId(headers, DEFAULT_CORRELATION_ID);
  }

  @Test
  public void requestFlowWithCustomCorrelationId() throws Exception {
    CoreEvent event = flowRunner("requestFlowWithCustomCorrelationId")
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();

    assertThat(event.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
    assertCorrelationId(headers, CUSTOM_CORRELATION_ID);
  }

  @Test
  public void clashingCorrelationIds() throws Exception {
    CoreEvent event = flowRunner("clashingCorrelationIds")
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();

    assertThat(event.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
    assertCorrelationId(headers, CUSTOM_CORRELATION_ID);
  }

  @Test
  public void neverSendCorrelationId() throws Exception {
    CoreEvent event = flowRunner("neverSendCorrelationId")
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();

    assertThat(event.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
    assertThat(headers.containsKey(X_CORRELATION_ID), is(false));
  }

  @Test
  public void usesListenerXCorrelationId() throws Exception {
    propagatesListenerCorrelationIdHeader(X_CORRELATION_ID);
  }

  @Test
  public void usesListenerMuleCorrelationId() throws Exception {
    propagatesListenerCorrelationIdHeader(MULE_CORRELATION_ID_PROPERTY);
  }

  private void propagatesListenerCorrelationIdHeader(String listenerCorrelationIdHeader) throws IOException {
    Request.Post(format("http://localhost:%s/", serverPort.getValue()))
        .addHeader(listenerCorrelationIdHeader, LISTENER_CORRELATION_ID)
        .execute();

    assertCorrelationId(headers, LISTENER_CORRELATION_ID);
  }

  private void assertCorrelationId(Multimap<String, String> headers, String expected) {
    Collection<String> values = headers.get(X_CORRELATION_ID);
    assertThat(values, hasSize(1));
    assertThat(values.iterator().next(), equalTo(expected));
  }

}
