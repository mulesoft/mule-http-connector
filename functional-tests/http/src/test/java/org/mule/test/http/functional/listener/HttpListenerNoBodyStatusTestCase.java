/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.extension.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.sdk.api.http.HttpConstants.HttpStatus.NOT_MODIFIED;
import static org.mule.sdk.api.http.HttpConstants.HttpStatus.NO_CONTENT;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.sdk.api.http.HttpConstants.HttpStatus;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunnerDelegateTo(Parameterized.class)
public class HttpListenerNoBodyStatusTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Parameter
  public HttpStatus status;

  @Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {{NO_CONTENT}, {NOT_MODIFIED}});
  }

  @Override
  protected String getConfigFile() {
    return "http-listener-no-content-config.xml";
  }

  @Test
  public void noBodyWhenEmpty() throws IOException {
    verifyResponseFrom("empty");
  }

  @Test
  public void noBodyWhenString() throws IOException {
    verifyResponseFrom("content");
  }

  private void verifyResponseFrom(String path) throws IOException {
    final Response response = Request.Get(getUrl(path)).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getEntity(), is(nullValue()));
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(status.getStatusCode()));
    assertThat(httpResponse.getFirstHeader(TRANSFER_ENCODING), is(nullValue()));
  }

  private String getUrl(String path) {
    return String.format("http://localhost:%s/%s?status=%s", port.getNumber(), path, status.getStatusCode());
  }

  public static class StreamingProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      return CoreEvent.builder(event)
          .message(Message.builder(event.getMessage()).value(new ByteArrayInputStream(new byte[] {})).build())
          .build();
    }
  }

}
