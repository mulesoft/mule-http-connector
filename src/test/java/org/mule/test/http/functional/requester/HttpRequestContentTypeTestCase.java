/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mule.extension.http.api.HttpMessageBuilder.refreshSystemProperties;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.api.metadata.MediaType.BINARY;
import static org.mule.runtime.api.metadata.MediaType.HTML;
import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.CONTENT;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.util.func.CheckedConsumer;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import io.qameta.allure.Story;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@Story(CONTENT)
public class HttpRequestContentTypeTestCase extends AbstractHttpRequestTestCase {

  private static final String EXPECTED_CONTENT_TYPE = "application/json; charset=UTF-8";

  private CheckedConsumer<HttpServletResponse> responder;

  @Rule
  public SystemProperty strictContentType =
      new SystemProperty(SYSTEM_PROPERTY_PREFIX + "strictContentType", TRUE.toString());

  @Override
  protected String getConfigFile() {
    return "http-request-content-type-config.xml";
  }

  @Before
  public void setUp() {
    refreshSystemProperties();
    responder = super::writeResponse;
  }

  @After
  public void tearDown() {
    refreshSystemProperties();
  }

  @Test
  public void sendsContentTypeOnRequest() throws Exception {
    verifyContentTypeForFlow("requesterContentType", "POST");
  }

  @Test
  public void sendsContentTypeOnRequestWithAlways() throws Exception {
    verifyContentTypeForFlow("requesterContentTypeAlways", "GET");
  }

  @Test
  public void doesNotSendContentTypeOnRequestWithEmpty() throws Exception {
    verifyNoContentTypeForFlow("requesterContentType", "GET");
  }

  @Test
  public void doesNotSendContentTypeOnRequestWithNever() throws Exception {
    verifyNoContentTypeForFlow("requesterContentTypeNever", "POST");
  }

  @Test
  public void sendsContentTypeOnRequestBuilder() throws Exception {
    verifyContentTypeForFlow("requesterBuilderContentType", "POST");
  }

  @Test
  public void returnsContentTypeWhenAvailable() throws Exception {
    TypedValue<Object> result = runFlow("requesterContentType", "POST").getPayload();
    assertThat(result.getDataType().getMediaType(), is(HTML.withCharset(ISO_8859_1)));
  }

  @Test
  public void returnsDefaultContentTypeWhenMissing() throws Exception {
    responder = response -> {
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().print(DEFAULT_RESPONSE);
    };

    TypedValue<Object> result = runFlow("requesterContentType", "POST").getPayload();
    assertThat(result.getDataType().getMediaType(), is(BINARY.withCharset(UTF_8)));
  }

  @Test
  public void returnsAnyContentTypeWhenEmpty() throws Exception {
    responder = response -> response.setStatus(HttpServletResponse.SC_OK);

    TypedValue<Object> result = runFlow("requesterContentType", "POST").getPayload();
    assertThat(result.getDataType().getMediaType(), is(ANY.withCharset(UTF_8)));
  }

  public void verifyContentTypeForFlow(String flowName, String method) throws Exception {
    runFlow(flowName, method);
    assertThat(getFirstReceivedHeader(CONTENT_TYPE.toLowerCase()), equalTo(EXPECTED_CONTENT_TYPE));
  }

  public void verifyNoContentTypeForFlow(String flowName, String method) throws Exception {
    runFlow(flowName, method);
    assertThat(headers.get(CONTENT_TYPE.toLowerCase()), is(empty()));
  }

  private Message runFlow(String flowName, String method) throws Exception {
    return flowRunner(flowName).withPayload(TEST_MESSAGE).withVariable("method", method).run().getMessage();
  }

  @Override
  protected void writeResponse(HttpServletResponse response) throws IOException {
    responder.accept(response);
  }

}
