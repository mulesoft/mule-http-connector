/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.internal.listener;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;

import org.mule.extension.http.internal.listener.HttpListenerProvider;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.http.api.server.HttpServer;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.lang.reflect.Field;
import java.net.BindException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpListenerProviderTestCase extends AbstractMuleTestCase {

  @Mock
  HttpServer server;

  @InjectMocks
  HttpListenerProvider httpListenerProvider = new HttpListenerProvider();

  @Test
  public void testWhenExceptionIsThrownItShouldBeWrappedAsMuleException() throws Exception {
    HttpListenerProvider.ConnectionParams connectionParams = new HttpListenerProvider.ConnectionParams();
    Field portField = HttpListenerProvider.ConnectionParams.class.getDeclaredField("port");
    portField.setAccessible(true);
    portField.set(connectionParams, 8081);

    Field connectionParamsField = HttpListenerProvider.class.getDeclaredField("connectionParams");
    connectionParamsField.setAccessible(true);
    connectionParamsField.set(httpListenerProvider, connectionParams);

    Field configNameField = HttpListenerProvider.class.getDeclaredField("configName");
    configNameField.setAccessible(true);
    configNameField.set(httpListenerProvider, "testConfig");

    doThrow(new BindException("Address already in use")).when(server).start();

    try {
      httpListenerProvider.start();
      fail("Was expecting start to fail");
    } catch (DefaultMuleException e) {
      assertThat(e.getMessage(), is("Could not start HTTP server for 'testConfig' on port 8081: Address already in use"));
    }
  }
}
