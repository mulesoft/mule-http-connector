/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mule.functional.junit4.TestLegacyMessageUtils.getInboundProperty;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.Rule;
import org.junit.Test;

public class HttpRequestFunctionalTestCase extends AbstractHttpRequestTestCase {

  private static final String TEST_HEADER_NAME = "TestHeaderName";
  private static final String TEST_HEADER_VALUE = "TestHeaderValue";

  @Rule
  public DynamicPort blockingHttpPort = new DynamicPort("blockingHttpPort");

  @Override
  protected String getConfigFile() {
    return "http-request-functional-config.xml";
  }

  @Test
  public void payloadIsSentAsRequestBody() throws Exception {
    flowRunner("requestFlow").withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).run();
    assertThat(body, equalTo(AbstractMuleContextTestCase.TEST_MESSAGE));
  }

  @Test
  public void responseBodyIsMappedToPayload() throws Exception {
    CoreEvent event = flowRunner("requestFlow").withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).run();
    assertThat(event.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
  }

  @Test
  public void blockingResponseBodyIsMappedToPayload() throws Exception {
    CoreEvent event = flowRunner("blockingRequestFlow").withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).run();
    assertTrue(event.getMessage().getPayload().getValue() instanceof String);
    assertThat(event.getMessage().getPayload().getValue(), equalTo("value"));
  }

  @Test
  public void responseStatusCodeIsSetAsInboundProperty() throws Exception {
    CoreEvent event = flowRunner("requestFlow").withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).run();
    assertThat((HttpResponseAttributes) event.getMessage().getAttributes().getValue(), HttpMessageAttributesMatchers
        .hasStatusCode(OK.getStatusCode()));
  }

  @Test
  public void responseHeadersAreMappedInAttributes() throws Exception {
    CoreEvent event = flowRunner("requestFlow").withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).run();
    HttpResponseAttributes responseAttributes = (HttpResponseAttributes) event.getMessage().getAttributes().getValue();
    assertThat(responseAttributes.getHeaders(), hasEntry(TEST_HEADER_NAME.toLowerCase(), TEST_HEADER_VALUE));
  }

  @Test
  public void basePathFromConfigIsUsedInRequest() throws Exception {
    flowRunner("requestFlow").withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).run();
    assertThat(uri, equalTo("/basePath/requestPath"));
  }

  @Test
  public void previousInboundPropertiesAreCleared() throws Exception {
    CoreEvent event =
        flowRunner("requestFlow").withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
            .withInboundProperty("TestInboundProperty", "TestValue").run();
    assertThat(getInboundProperty(event.getMessage(), "TestInboundProperty"), nullValue());
  }

  @Test
  public void sendProperties() throws Exception {
    flowRunner("requestPropertiesFlow").withPayload(AbstractMuleContextTestCase.TEST_MESSAGE).run();
    assertThat(uri, equalTo("/basePath/request/1/2?query1=4&query2=5"));
    assertThat(getFirstReceivedHeader("X-Custom-Int"), is("5"));
    assertThat(getFirstReceivedHeader("X-Custom-String"), is("6"));
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.addHeader(TEST_HEADER_NAME, TEST_HEADER_VALUE);
    super.handleRequest(baseRequest, request, response);
  }
}
