/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static java.lang.String.format;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.functional.api.component.FunctionalTestProcessor.getFromFlow;
import org.mule.runtime.api.metadata.DataType;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.InputStreamEntity;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpListenerRequestStreamingTestCase extends AbstractHttpTestCase {

  private static final String LARGE_MESSAGE = RandomStringUtils.randomAlphanumeric(100 * 1024);

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");
  private String flowReceivedMessage;

  @Override
  protected String getConfigFile() {
    return "http-listener-request-streaming-config.xml";
  }

  @Test
  public void listenerReceivedChunkedRequest() throws Exception {
    String url = format("http://localhost:%s/", listenPort.getNumber());
    getFromFlow(locator, "defaultFlow")
        .setEventCallback((context, component, muleContext) -> flowReceivedMessage = muleContext.getTransformationService()
            .transform(context.getMessage(), DataType.STRING).getPayload().getValue().toString());
    testChunkedRequestContentAndResponse(url);
    // We check twice to verify that the chunked request is consumed completely. Otherwise second request would fail
    testChunkedRequestContentAndResponse(url);
    testStreamClosed(url);
  }

  private void testChunkedRequestContentAndResponse(String url) throws Exception {
    Request.Post(url).body(new InputStreamEntity(new ByteArrayInputStream(LARGE_MESSAGE.getBytes()))).connectTimeout(1000)
        .execute();
    assertThat(flowReceivedMessage, is(LARGE_MESSAGE));
  }

  private void testStreamClosed(String url) throws Exception {
    InputStream requestInputStream = Mockito.mock(InputStream.class);
    when(requestInputStream.read(Mockito.any(byte[].class))).thenReturn(1).thenReturn(-1);
    InputStreamEntity body = new InputStreamEntity(requestInputStream);
    Request.Post(url).body(body).connectTimeout(1000)
        .execute();
    verify(requestInputStream).close();
  }

}
