/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.mule.functional.api.component.TestConnectorQueueHandler;
import org.mule.runtime.api.message.Message;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class HttpListenerEncodingTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("port");

  private static final String JAPANESE_MESSAGE = "\u3042";
  private static final String ARABIC_MESSAGE = "\u0634";
  private static final String CYRILLIC_MESSAGE = "\u0416";
  private static final String SIMPLE_MESSAGE = "A";

  private TestConnectorQueueHandler queueHandler;

  @Parameterized.Parameter(0)
  public String encoding;

  @Parameterized.Parameter(1)
  public String testMessage;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return asList(new Object[][] {
        {"EUC-JP", JAPANESE_MESSAGE},
        {"Windows-31J", JAPANESE_MESSAGE},
        {"ISO-2022-JP", JAPANESE_MESSAGE},
        {"UTF-8", JAPANESE_MESSAGE},
        {"Arabic", ARABIC_MESSAGE},
        {"Windows-1256", ARABIC_MESSAGE},
        {"Windows-1251", CYRILLIC_MESSAGE},
        {"Cyrillic", CYRILLIC_MESSAGE},
        {"US-ASCII", SIMPLE_MESSAGE}});
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    queueHandler = new TestConnectorQueueHandler(registry);
  }

  @Override
  protected String getConfigFile() {
    return "http-listener-encoding-config.xml";
  }

  @Test
  public void testEncoding() throws Exception {
    final String url = String.format("http://localhost:%s/test", port.getNumber());
    Charset charset = Charset.forName(encoding);
    Request request = Request.Post(url).bodyString(testMessage, ContentType.create("text/plain", charset));
    Response response = request.execute();
    assertThat(response.returnResponse().getFirstHeader(CONTENT_TYPE).getValue(),
               containsString("charset=" + charset.displayName()));
    Message message = queueHandler.read("out", 2000).getMessage();
    assertThat(getPayloadAsString(message), is(testMessage));
    assertThat(message.getPayload().getDataType().getMediaType().getCharset().get(), is(charset));
  }

}
