/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.api.HttpMessageBuilder.refreshSystemProperties;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import io.qameta.allure.Story;

@Story("Header case preservation")
public class HttpHeaderCaseTestCase extends AbstractHttpTestCase {

  public static final String PRESERVE_HEADER_CASE = "org.glassfish.grizzly.http.PRESERVE_HEADER_CASE";

  @Rule
  public DynamicPort port = new DynamicPort("port");
  @Rule
  public SystemProperty headerCaseProperty = new SystemProperty(PRESERVE_HEADER_CASE, "true");

  @Override
  protected String getConfigFile() {
    return "http-header-case-config.xml";
  }

  @Before
  public void setUp() {
    refreshSystemProperties();
  }

  @After
  public void tearDown() {
    refreshSystemProperties();
  }

  @Test
  public void listenerPreservesRequestHeaderCase() throws ClientProtocolException, IOException {
    HttpResponse response = Request.Get(format("http://localhost:%s/testRequest", port.getNumber()))
        .addHeader("rEqUeStHeAdEr", "value").execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
  }

  @Test
  public void listenerPreservesResponseHeaderCase() throws ClientProtocolException, IOException {
    HttpResponse response = Request.Get(format("http://localhost:%s/testResponse", port.getNumber()))
        .execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
    Set<String> headerNames = stream(response.getAllHeaders()).map(h -> h.getName()).collect(toSet());
    assertThat(headerNames, hasItems("rEsPoNsEhEaDeR", "Transfer-Encoding"));
  }

  @Test
  public void requesterPreservesRequestHeaderCase() throws Exception {
    runFlow("clientRequest");
  }

  @Test
  public void requesterPreservesResponseHeaderCase() throws Exception {
    runFlow("clientResponse");
  }

  @Test
  public void proxyPreservesRequestHeaderCase() throws ClientProtocolException, IOException {
    HttpResponse response = Request.Get(format("http://localhost:%s/proxyRequest", port.getNumber()))
        .addHeader("pRoXyHeAdEr", "value").execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
    Set<String> headerNames = stream(response.getAllHeaders()).map(h -> h.getName()).collect(toSet());
    assertThat(headerNames, hasItems("pRoXyHeAdEr"));
  }

  @Test
  public void proxyPreservesResponseHeaderCase() throws ClientProtocolException, IOException {
    HttpResponse response = Request.Get(format("http://localhost:%s/proxyResponse", port.getNumber()))
        .execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
    Set<String> headerNames = stream(response.getAllHeaders()).map(h -> h.getName()).collect(toSet());
    assertThat(headerNames, hasItems("pRoXyHeAdErReSpOnSe"));
  }

  public static class AssertRequestHeaderProcessor extends AbstractComponent implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      HttpRequestAttributes attributes = (HttpRequestAttributes) event.getMessage().getAttributes().getValue();

      assertThat(attributes.getHeaders().keySet(), hasItems("rEqUeStHeAdEr", "User-Agent"));

      return event;
    }
  }

  public static class AssertResponseHeaderProcessor extends AbstractComponent implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      HttpResponseAttributes attributes = (HttpResponseAttributes) event.getMessage().getAttributes().getValue();

      assertThat(attributes.getHeaders().keySet(), hasItems("rEsPoNsEhEaDeR", "Content-Length"));

      return event;
    }
  }

}
