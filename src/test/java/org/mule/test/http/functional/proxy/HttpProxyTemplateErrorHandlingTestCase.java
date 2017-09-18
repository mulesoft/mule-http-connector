/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.proxy;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.PROXY;

import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.SensingNullMessageProcessor;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(HTTP_EXTENSION)
@Story(PROXY)
public class HttpProxyTemplateErrorHandlingTestCase extends AbstractHttpRequestTestCase {

  public static final String SERVICE_DOWN_MESSAGE = "Service Down";
  public static final String CATCH_SENSING_PROCESSOR_NAME = "catchSensingMessageProcessor";
  public static final String ROLLBACK_SENSING_PROCESSOR_NAME = "rollbackSensingMessageProcessor";

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");

  @Inject
  @Named(CATCH_SENSING_PROCESSOR_NAME)
  private SensingNullMessageProcessor catchSensingProcessor;

  @Inject
  @Named(ROLLBACK_SENSING_PROCESSOR_NAME)
  private SensingNullMessageProcessor rollbackSensingProcessor;

  @Override
  protected boolean doTestClassInjection() {
    return true;
  }

  @Override
  protected String getConfigFile() {
    return "http-proxy-template-error-handling-config.xml";
  }

  @Override
  @Before
  public void startServer() throws Exception {
    // Don't start server so that requests fail
  }

  @Override
  @After
  public void stopServer() throws Exception {
    // No server to stop
  }

  @Test
  public void noExceptionStrategy() throws Exception {
    HttpResponse response =
        Request.Get(getProxyUrl("noExceptionStrategy")).connectTimeout(RECEIVE_TIMEOUT).execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(500));
  }

  @Test
  public void catchExceptionStrategy() throws Exception {
    HttpResponse response =
        Request.Get(getProxyUrl("catchExceptionStrategy")).connectTimeout(RECEIVE_TIMEOUT).execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(200));
    assertThat(IOUtils.toString(response.getEntity().getContent()), equalTo(SERVICE_DOWN_MESSAGE));

    assertThat(catchSensingProcessor.event, is(notNullValue()));
  }

  @Test
  public void rollbackExceptionStrategy() throws Exception {
    HttpResponse response =
        Request.Get(getProxyUrl("rollbackExceptionStrategy")).connectTimeout(RECEIVE_TIMEOUT).execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(500));
    assertThat(IOUtils.toString(response.getEntity().getContent()), not(equalTo(SERVICE_DOWN_MESSAGE)));

    assertThat(rollbackSensingProcessor.event, is(notNullValue()));
  }

  private String getProxyUrl(String path) {
    return String.format("http://localhost:%s/%s", proxyPort.getNumber(), path);
  }

}
