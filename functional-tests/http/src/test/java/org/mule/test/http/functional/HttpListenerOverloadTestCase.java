/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.INTERNAL_SERVER_ERROR;

import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;

import io.qameta.allure.Story;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;

@Story("Overload response status")
public class HttpListenerOverloadTestCase extends AbstractHttpTestCase {

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

    assertThat(response.getStatusLine().getStatusCode(), is(INTERNAL_SERVER_ERROR.getStatusCode()));
    assertThat(response.getStatusLine().getReasonPhrase(), is(INTERNAL_SERVER_ERROR.getReasonPhrase()));
    assertThat(IOUtils.toString(response.getEntity().getContent()), is("Scheduler unavailable"));
  }

}
