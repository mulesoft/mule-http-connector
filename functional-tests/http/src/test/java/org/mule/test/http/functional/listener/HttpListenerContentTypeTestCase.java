/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.CONTENT;

import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;

import io.qameta.allure.Story;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.junit.Rule;
import org.junit.Test;

@Story(CONTENT)
public class HttpListenerContentTypeTestCase extends AbstractHttpTestCase {

  private static final String EXPECTED_CONTENT_TYPE = "application/json; charset=UTF-8";

  @Rule
  public SystemProperty strictContentType =
      new SystemProperty(SYSTEM_PROPERTY_PREFIX + "strictContentType", Boolean.TRUE.toString());

  @Rule
  public DynamicPort httpPort = new DynamicPort("httpPort");

  @Override
  protected String getConfigFile() {
    return "http-listener-content-type-config.xml";
  }

  @Test
  public void returnsContentTypeInResponse() throws Exception {
    HttpResponse response = Request.Post(getUrl()).body(new StringEntity(TEST_MESSAGE, TEXT_PLAIN)).execute().returnResponse();

    assertContentTypeProperty(response, EXPECTED_CONTENT_TYPE);
  }

  @Test
  public void returnsContentTypeInResponseFromBuilder() throws Exception {
    HttpResponse response =
        Request.Post(getUrl("testBuilder")).body(new StringEntity(TEST_MESSAGE, TEXT_PLAIN)).execute().returnResponse();

    assertContentTypeProperty(response, "text/plain");
  }

  @Test
  public void returnsNoContentTypeWhenPayloadEmpty() throws Exception {
    HttpResponse response =
        Request.Post(getUrl("testEmpty")).body(new StringEntity(TEST_MESSAGE, TEXT_PLAIN)).execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
    assertThat(response.getFirstHeader(CONTENT_TYPE), is(nullValue()));
  }

  @Test
  public void rejectsInvalidContentTypeWithoutBody() throws Exception {
    Request request = Request.Post(getUrl()).addHeader(CONTENT_TYPE, "application");
    testRejectContentType(request, "MediaType cannot be parsed: application");
  }

  @Test
  public void rejectsInvalidContentTypeWithBody() throws Exception {
    Request request = Request.Post(getUrl()).body(new StringEntity(TEST_MESSAGE, "application", null));
    testRejectContentType(request, "MediaType cannot be parsed: application");
  }

  @Test
  public void rejectsAndEscapesInvalidContentTypeWithScript() throws Exception {
    Request request = Request.Post(getUrl()).body(new StringEntity(TEST_MESSAGE, "<script>alert('Hello');</script>", null));
    testRejectContentType(request, "MediaType cannot be parsed: &lt;script&gt;alert('Hello');&lt;/script&gt;;");
  }

  private void testRejectContentType(Request request, String expectedMessage) throws IOException {
    HttpResponse response = request.execute().returnResponse();
    StatusLine statusLine = response.getStatusLine();

    assertThat(IOUtils.toString(response.getEntity().getContent()), containsString(expectedMessage));
    assertThat(statusLine.getStatusCode(), is(BAD_REQUEST.getStatusCode()));
    assertThat(statusLine.getReasonPhrase(), is(BAD_REQUEST.getReasonPhrase()));
  }

  private String getUrl() {
    return getUrl("testInput");
  }

  private String getUrl(String path) {
    return String.format("http://localhost:%s/%s", httpPort.getValue(), path);
  }

  private void assertContentTypeProperty(HttpResponse response, String expectedContentType) {
    String contentType = response.getFirstHeader(CONTENT_TYPE).getValue();
    assertThat(contentType, notNullValue());
    assertThat(contentType, equalTo(expectedContentType));
  }

  public static class NullifyingProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      return CoreEvent.builder(event).message(Message.builder(event.getMessage()).nullValue().mediaType(ANY).build()).build();
    }

  }
}
