/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.http.client.fluent.Request.Get;
import static org.apache.http.client.fluent.Request.Post;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.sdk.api.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.sdk.api.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.sdk.api.http.HttpHeaders.Values.CHUNKED;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Response;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Probe;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class HttpListenerResponseStreamingTestCase extends AbstractHttpTestCase {

  private static final int DEFAULT_TIMEOUT = 10000;

  private static final int POOLING_FREQUENCY_MILLIS = 1000;
  private static final int POOLING_TIMEOUT_MILLIS = 20000;

  @ClassRule
  public static SystemProperty stringPayload =
      new SystemProperty("stringPayload", randomAlphabetic(getDefaultEnvironmentConfiguration().getRandomCount()));


  @ClassRule
  public static SystemProperty mapPayload = new SystemProperty("mapPayload", "one=1&two=2");

  private static InputStreamWrapper testStream;

  @Rule
  public SystemProperty stringPayloadLength =
      new SystemProperty("stringPayloadLength", String.valueOf(stringPayload.getValue().length()));

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  protected abstract HttpVersion getHttpVersion();

  @Before
  public void setUp() {
    testStream = null;
  }

  @Override
  protected String getConfigFile() {
    return "http-listener-response-streaming-config.xml";
  }

  protected String getUrl(String path) {
    return String.format("http://localhost:%s/%s", listenPort.getNumber(), path);
  }

  protected void testResponseIsContentLengthEncoding(String url, HttpVersion httpVersion) throws IOException {
    testResponseIsContentLengthEncoding(url, httpVersion, stringPayload.getValue());
  }

  protected void testResponseIsChunkedEncoding(String url, HttpVersion httpVersion) throws IOException {
    testResponseIsChunkedEncoding(url, httpVersion, stringPayload.getValue());
  }

  protected void testResponseIsNotChunkedEncoding(String url, HttpVersion httpVersion) throws IOException {
    testResponseIsNotChunkedEncoding(url, httpVersion, stringPayload.getValue());
  }

  protected void testResponseIsContentLengthEncoding(String url, HttpVersion httpVersion, String expectedBody)
      throws IOException {
    final HttpResponse httpResponse = verifyIsContentLength(url, httpVersion);
    assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), is(expectedBody));
  }

  protected void testResponseIsChunkedEncoding(String url, HttpVersion httpVersion, String expectedBody) throws IOException {
    final Response response = Post(url).version(httpVersion).connectTimeout(DEFAULT_TIMEOUT).socketTimeout(DEFAULT_TIMEOUT)
        .bodyByteArray(expectedBody.getBytes()).execute();
    final HttpResponse httpResponse = response.returnResponse();
    verifyIsChunked(httpResponse);
    assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), is(expectedBody));
  }

  protected void testResponseIsNotChunkedEncoding(String url, HttpVersion httpVersion, String expectedBody) throws IOException {
    final Response response = Post(url).version(httpVersion).connectTimeout(DEFAULT_TIMEOUT).socketTimeout(DEFAULT_TIMEOUT)
        .bodyByteArray(expectedBody.getBytes()).execute();
    final HttpResponse httpResponse = response.returnResponse();
    verifyIsNotChunked(httpResponse);
    assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), is(expectedBody));
  }

  protected void streamIsClosed() {
    new PollingProber(POOLING_TIMEOUT_MILLIS, POOLING_FREQUENCY_MILLIS).check(new Probe() {

      @Override
      public boolean isSatisfied() {
        return testStream.isClosed();
      }

      @Override
      public String describeFailure() {
        return "The test stream was not closed.";
      }
    });
  }

  private HttpResponse verifyIsContentLength(String url, HttpVersion httpVersion) throws IOException {
    final Response response =
        Get(url).version(httpVersion).connectTimeout(DEFAULT_TIMEOUT).socketTimeout(DEFAULT_TIMEOUT).execute();
    final HttpResponse httpResponse = response.returnResponse();
    final Header transferEncodingHeader = httpResponse.getFirstHeader(TRANSFER_ENCODING);
    final Header contentLengthHeader = httpResponse.getFirstHeader(CONTENT_LENGTH);
    assertThat(contentLengthHeader, notNullValue());
    assertThat(transferEncodingHeader, nullValue());
    return httpResponse;
  }

  private void verifyIsChunked(HttpResponse httpResponse) throws IOException {
    final Header transferEncodingHeader = httpResponse.getFirstHeader(TRANSFER_ENCODING);
    final Header contentLengthHeader = httpResponse.getFirstHeader(CONTENT_LENGTH);
    assertThat(contentLengthHeader, nullValue());
    assertThat(transferEncodingHeader, notNullValue());
    assertThat(transferEncodingHeader.getValue(), is(CHUNKED));
  }

  private void verifyIsNotChunked(HttpResponse httpResponse) throws IOException {
    final Header transferEncodingHeader = httpResponse.getFirstHeader(TRANSFER_ENCODING);
    final Header contentLengthHeader = httpResponse.getFirstHeader(CONTENT_LENGTH);
    assertThat(contentLengthHeader, nullValue());
    assertThat(transferEncodingHeader, is(nullValue()));
  }

  public static class InputStreamWrapper extends ByteArrayInputStream {

    boolean closed = false;

    public InputStreamWrapper(byte[] buf) {
      super(buf);
    }

    @Override
    public void close() throws IOException {
      super.close();
      closed = true;
    }

    public boolean isClosed() {
      return closed;
    }
  }

  public static class StreamProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent coreEvent) throws MuleException {
      testStream = new InputStreamWrapper(stringPayload.getValue().getBytes());
      return CoreEvent.builder(coreEvent).message(of(testStream)).build();
    }
  }

}
