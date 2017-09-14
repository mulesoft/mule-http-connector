/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.BaseEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import io.qameta.allure.Feature;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
public class BasicHttpTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  protected String getConfigFile() {
    return "basic-http-config.xml";
  }

  @Test
  public void sendsRequest() throws Exception {
    BaseEvent response = flowRunner("client").withPayload("PEPE").run();
    assertThat(response.getMessage().getPayload().getValue(), is(DEFAULT_RESPONSE));
    assertThat(method, is("GET"));
    assertThat(headers.get("X-Custom"), contains("custom-value"));
    assertThat(uri, is("/?query=param"));
  }

  @Test
  public void receivesRequest() throws Exception {
    HttpGet getRequest = new HttpGet(String.format("http://localhost:%s/test?query=param", serverPort.getValue()));
    getRequest.addHeader("Y-Custom", "value-custom");
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
        assertThat(IOUtils.toString(response.getEntity().getContent()), is("HEY"));
      }
    }
  }

  @Test
  public void invalidError() throws Exception {
    HttpGet getRequest = new HttpGet(String.format("http://localhost:%s/invalid?query=param", serverPort.getValue()));
    getRequest.addHeader("Y-Custom", "value-custom");
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
        assertThat(response.getStatusLine().getStatusCode(), is(500));
        assertThat(response.getStatusLine().getReasonPhrase(), is("Server Error"));
        assertThat(IOUtils.toString(response.getEntity().getContent()),
                   is(containsString("Script \'error.description ++ \' has errors")));
      }
    }
  }

  protected static class RequestCheckerMessageProcessor implements Processor {

    @Override
    public BaseEvent process(BaseEvent event) throws MuleException {
      Message message = event.getMessage();
      Object payload = message.getPayload().getValue();
      assertThat(payload, is(notNullValue()));
      assertThat(message.getAttributes().getValue(), instanceOf(HttpRequestAttributes.class));
      HttpRequestAttributes requestAttributes = (HttpRequestAttributes) message.getAttributes().getValue();
      assertThat(requestAttributes.getMethod(), is("GET"));
      assertThat(requestAttributes.getScheme(), is("http"));
      assertThat(requestAttributes.getVersion(), is("HTTP/1.1"));
      assertThat(requestAttributes.getRequestUri(), is("/test?query=param"));
      assertThat(requestAttributes.getListenerPath(), is("/test"));
      assertThat(requestAttributes.getQueryString(), is("query=param"));
      assertThat(requestAttributes.getQueryParams(), hasEntry("query", "param"));
      assertThat(requestAttributes.getHeaders(), hasEntry("y-custom", "value-custom"));
      return event;
    }
  }
}
