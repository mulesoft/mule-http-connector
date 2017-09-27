/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.MULTI_MAP;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.functional.api.component.TestConnectorQueueHandler;
import org.mule.runtime.api.message.Message;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.StringEntity;
import org.junit.Rule;
import org.junit.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(HTTP_EXTENSION)
@Story(MULTI_MAP)
public class HttpListenerMultipleValueHeadersTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("port");

  private static final String HEADER = "multipleheader";
  private TestConnectorQueueHandler queueHandler;

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    queueHandler = new TestConnectorQueueHandler(registry);
  }

  @Override
  protected String getConfigFile() {
    return "http-listener-multiple-header-config.xml";
  }

  @Test
  public void sendMultipleValuedHeader() throws Exception {
    final Response response = Request.Get(getUrl()).execute();
    HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
    Header[] headers = httpResponse.getHeaders(HEADER);
    assertThat(headers, arrayWithSize(3));
    assertThat(stream(headers).map(Header::getValue).collect(toList()), contains("1", "2", "3"));
  }

  @Test
  public void receivesMultipleValuedHeader() throws Exception {
    final Response response = Request.Post(getUrl())
        .body(new StringEntity(TEST_MESSAGE))
        .addHeader(HEADER, "1")
        .addHeader(HEADER, "2")
        .addHeader(HEADER, "3")
        .execute();
    HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

    Message message = queueHandler.read("out", RECEIVE_TIMEOUT).getMessage();

    assertThat(message.getAttributes().getValue(), instanceOf(HttpRequestAttributes.class));
    List<String> headers = ((HttpRequestAttributes) message.getAttributes().getValue()).getHeaders().getAll(HEADER);
    assertThat(headers, hasSize(3));
    assertThat(headers, contains("1", "2", "3"));
  }

  private String getUrl() {
    return format("http://localhost:%s/test", port.getValue());
  }

}
