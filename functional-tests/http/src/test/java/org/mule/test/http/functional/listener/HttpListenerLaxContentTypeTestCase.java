/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.CONTENT;

import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;

import io.qameta.allure.Story;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;

@Story(CONTENT)
public class HttpListenerLaxContentTypeTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort httpPort = new DynamicPort("httpPort");

  @Override
  protected String getConfigFile() {
    return "http-listener-lax-content-type-config.xml";
  }

  @Test
  public void ignoresInvalidContentTypeWithoutBody() throws Exception {
    Request request = Request.Post(getUrl()).addHeader(CONTENT_TYPE, "application");
    testIgnoreInvalidContentType(request, "{ \"key1\" : \"value, \"key2\" : 2 }");
  }

  @Test
  public void returnsInvalidContentTypeInResponse() throws Exception {
    HttpResponse response = Request.Post(getUrlForInvalid()).execute().returnResponse();
    assertInvalidContentTypeHeader(response);
  }

  private void testIgnoreInvalidContentType(Request request, String expectedMessage) throws IOException {
    HttpResponse response = request.execute().returnResponse();
    StatusLine statusLine = response.getStatusLine();

    assertThat(IOUtils.toString(response.getEntity().getContent()), containsString(expectedMessage));
    assertThat(statusLine.getStatusCode(), is(OK.getStatusCode()));
  }

  private String getUrl() {
    return String.format("http://localhost:%s/testInput", httpPort.getValue());
  }

  private String getUrlForInvalid() {
    return String.format("http://localhost:%s/testInputInvalid", httpPort.getValue());
  }

  private void assertInvalidContentTypeHeader(HttpResponse response) {
    Header contentTypeHeader = response.getFirstHeader(CONTENT_TYPE);
    assertThat(contentTypeHeader, is(notNullValue()));
    assertThat(contentTypeHeader.getValue(), is("invalidMimeType"));
  }
}
