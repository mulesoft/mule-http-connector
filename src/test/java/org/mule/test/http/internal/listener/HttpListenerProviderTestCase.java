/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.internal.listener;

import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.ERROR_HANDLING;

import static org.mockito.Mockito.doThrow;

import org.mule.extension.http.internal.listener.HttpListenerProvider;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.http.api.server.HttpServer;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.lang.reflect.Field;
import java.net.BindException;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Feature(HTTP_EXTENSION)
@Story(ERROR_HANDLING)
@Issue("W-11090837")
public class HttpListenerProviderTestCase extends AbstractMuleTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  private HttpServer server;

  @InjectMocks
  private HttpListenerProvider httpListenerProvider = new HttpListenerProvider();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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

    expectedException.expect(DefaultMuleException.class);
    expectedException.expectMessage("Could not start HTTP server for 'testConfig' on port 8081: Address already in use");

    httpListenerProvider.start();
  }
}
