/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.api.error.HttpError.CONNECTIVITY;
import static org.mule.extension.http.internal.listener.HttpListener.HTTP_NAMESPACE;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.functional.junit4.rules.ExpectedError.none;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.runtime.http.api.HttpConstants.Method.POST;
import static org.mule.runtime.http.api.HttpHeaders.Names.HOST;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import org.mule.functional.junit4.rules.ExpectedError;
import org.mule.runtime.core.api.event.BaseEvent;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import io.qameta.allure.Feature;

@Feature(HTTP_EXTENSION)
public class HttpRequestDynamicConfigTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public ExpectedError expectedError = none();

  @Rule
  public DynamicPort unusedPort = new DynamicPort("unusedPort");

  @Override
  protected String getConfigFile() {
    return "http-request-dynamic-configs.xml";
  }

  @Test
  public void requestsGoThroughClient1() throws Exception {
    BaseEvent result = flowRunner("client1")
        .withVariable("basePath", "api/v1")
        .withVariable("follow", true)
        .withVariable("send", "AUTO")
        .withVariable("host", "localhost")
        .withVariable("path", "clients")
        .withVariable("method", GET)
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();
    assertThat(result.getMessage(), hasPayload(is(DEFAULT_RESPONSE)));
    assertThat(method, is(GET.toString()));
    assertThat(uri, is("/api/v1/clients"));
    assertThat(body, is(""));
    assertThat(headers.keys(), hasItem(HOST));
    assertThat(headers.get(HOST), hasItem(containsString("localhost:")));

    result = flowRunner("client1")
        .withVariable("basePath", "api/v2")
        .withVariable("follow", true)
        .withVariable("send", "ALWAYS")
        .withVariable("host", "localhost")
        .withVariable("path", "items")
        .withVariable("method", GET)
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();
    assertThat(result.getMessage(), hasPayload(is(DEFAULT_RESPONSE)));
    assertThat(method, is(GET.toString()));
    assertThat(uri, is("/api/v2/items"));
    assertThat(body, Matchers.is(AbstractMuleContextTestCase.TEST_MESSAGE));
    assertThat(headers.keys(), hasItem(HOST));
    assertThat(headers.get(HOST), hasItem(containsString("localhost:")));
  }

  @Test
  public void requestsGoThroughClient2() throws Exception {
    BaseEvent result = flowRunner("client2")
        .withVariable("parse", false)
        .withVariable("stream", "AUTO")
        .withVariable("timeout", 20000)
        .withVariable("port", httpPort.getNumber())
        .withVariable("body", AbstractMuleTestCase.TEST_PAYLOAD)
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();
    assertThat(result.getMessage(), hasPayload(is(DEFAULT_RESPONSE)));
    assertThat(method, is(POST.toString()));
    assertThat(uri, is("/testPath"));
    assertThat(body, Matchers.is(AbstractMuleTestCase.TEST_PAYLOAD));
    assertThat(headers.keys(), not(hasItem(TRANSFER_ENCODING)));

    result = flowRunner("client2")
        .withVariable("parse", false)
        .withVariable("stream", "ALWAYS")
        .withVariable("timeout", 20000)
        .withVariable("port", httpPort.getNumber())
        .withVariable("body", AbstractMuleTestCase.TEST_PAYLOAD)
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();
    assertThat(result.getMessage(), hasPayload(is(DEFAULT_RESPONSE)));
    assertThat(method, is(POST.toString()));
    assertThat(uri, is("/testPath"));
    assertThat(body, Matchers.is(AbstractMuleTestCase.TEST_PAYLOAD));
    assertThat(headers.keys(), hasItem(TRANSFER_ENCODING));
  }

  @Test
  public void requestWithDynamicConnectionParamUsesDifferentConfigs() throws Exception {
    BaseEvent result = flowRunner("client2")
        .withVariable("parse", false)
        .withVariable("stream", "AUTO")
        .withVariable("timeout", 20000)
        .withVariable("port", httpPort.getNumber())
        .withVariable("body", AbstractMuleTestCase.TEST_PAYLOAD)
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();
    assertThat(result.getMessage(), hasPayload(is(DEFAULT_RESPONSE)));
    assertThat(method, is(POST.toString()));
    assertThat(uri, is("/testPath"));
    assertThat(body, Matchers.is(AbstractMuleTestCase.TEST_PAYLOAD));
    assertThat(headers.keys(), not(hasItem(TRANSFER_ENCODING)));


    expectedError.expectErrorType(HTTP_NAMESPACE.toUpperCase(), CONNECTIVITY.name());
    expectedError.expectMessage(containsString("Connection refused."));
    flowRunner("client2")
        .withVariable("parse", false)
        .withVariable("stream", "AUTO")
        .withVariable("timeout", 20000)
        .withVariable("port", unusedPort.getNumber())
        .withVariable("body", AbstractMuleTestCase.TEST_PAYLOAD)
        .withPayload(AbstractMuleContextTestCase.TEST_MESSAGE)
        .run();
  }
}
