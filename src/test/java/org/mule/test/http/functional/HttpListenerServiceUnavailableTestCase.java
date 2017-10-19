/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;

import io.qameta.allure.Feature;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
public class HttpListenerServiceUnavailableTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  @Override
  protected String getConfigFile() {
    return "http-listener-service-unavailable.xml";
  }

  @Test
  public void returnsServerBusy() throws Exception {
    final String url = format("http://localhost:%s/", listenPort.getNumber());
    final HttpResponse response = Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(SERVICE_UNAVAILABLE.getStatusCode()));
    assertThat(response.getStatusLine().getReasonPhrase(), is(SERVICE_UNAVAILABLE.getReasonPhrase()));
    assertThat(IOUtils.toString(response.getEntity().getContent()), is("Scheduler unavailable"));
  }

}
