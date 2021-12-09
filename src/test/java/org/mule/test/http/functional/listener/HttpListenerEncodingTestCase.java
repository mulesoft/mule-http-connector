/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.api.config.i18n.CoreMessages.errorReadingStream;
import static org.mule.runtime.core.api.util.IOUtils.copyLarge;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mule.functional.api.component.TestConnectorQueueHandler;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.transformer.AbstractTransformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.api.util.StringMessageUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

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
    return Arrays.asList(new Object[][] {{"EUC-JP", JAPANESE_MESSAGE}, {"Windows-31J", JAPANESE_MESSAGE},
        {"ISO-2022-JP", JAPANESE_MESSAGE}, {"UTF-8", JAPANESE_MESSAGE}, {"Arabic", ARABIC_MESSAGE},
        {"Windows-1256", ARABIC_MESSAGE}, {"Windows-1251", CYRILLIC_MESSAGE}, {"Cyrillic", CYRILLIC_MESSAGE},
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

  public static class ObjectToStringProcessor extends AbstractTransformer {

    public ObjectToStringProcessor() {
      registerSourceType(DataType.CURSOR_STREAM_PROVIDER);
      setReturnDataType(DataType.STRING);
    }

    @Override
    public Object doTransform(Object src, Charset outputEncoding) throws TransformerException {
      if (src instanceof CursorStreamProvider) {
        return createStringFromInputStream(((CursorStreamProvider) src).openCursor(), outputEncoding);
      } else {
        return StringMessageUtils.toString(src);
      }
    }

    protected String createStringFromInputStream(InputStream input, Charset outputEncoding)
        throws TransformerException {
      try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        copyLarge(input, outputStream);
        return outputStream.toString(outputEncoding.name());
      } catch (IOException e) {
        throw new TransformerException(errorReadingStream(), e);
      } finally {
        try {
          input.close();
        } catch (IOException e) {
          logger.warn("Could not close stream", e);
        }
      }
    }
  }

}
