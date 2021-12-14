/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.MediaType.TEXT;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;

import org.junit.Rule;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.runner.RunnerDelegateTo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class HttpRequestEncodingTestCase extends AbstractHttpRequestTestCase {

  private static final String JAPANESE_MESSAGE = "\u3042";
  private static final String ARABIC_MESSAGE = "\u0634";
  private static final String CYRILLIC_MESSAGE = "\u0416";
  private static final String SIMPLE_MESSAGE = "A";

  public String encoding;

  public String testMessage;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {{"EUC-JP", JAPANESE_MESSAGE}, {"Windows-31J", JAPANESE_MESSAGE},
        {"ISO-2022-JP", JAPANESE_MESSAGE}, {"UTF-8", JAPANESE_MESSAGE}, {"Arabic", ARABIC_MESSAGE},
        {"Windows-1256", ARABIC_MESSAGE}, {"Windows-1251", CYRILLIC_MESSAGE}, {"Cyrillic", CYRILLIC_MESSAGE},
        {"US-ASCII", SIMPLE_MESSAGE}});
  }

  @Rule
  public SystemProperty testEncoding;

  public HttpRequestEncodingTestCase(String encoding, String testMessage) {
    this.encoding = encoding;
    this.testMessage = testMessage;
    this.testEncoding = new SystemProperty("testEncoding", encoding);
  }

  @Override
  protected String getConfigFile() {
    return "http-request-encoding-config.xml";
  }

  @Override
  protected void writeResponse(HttpServletResponse response) throws IOException {
    response.setHeader(CONTENT_TYPE, String.format("text/plain; charset=%s", encoding));
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().print(testMessage);
  }

  @Test
  public void testEncoding() throws Exception {
    Charset charset = Charset.forName(encoding);
    MediaType mediaType = TEXT.withCharset(charset);
    CoreEvent result = flowRunner("encodingTest")
        .keepStreamsOpen()
        .withPayload(testMessage)
        .withMediaType(mediaType)
        .keepStreamsOpen()
        .run();
    assertThat(getPayloadAsString(result.getMessage()), is(testMessage));
    assertThat(result.getMessage().getPayload().getDataType().getMediaType(), is(mediaType));
    assertThat(body, is(testMessage));
    assertThat(getFirstReceivedHeader(CONTENT_TYPE), containsString("charset=" + charset.displayName()));
  }

}
