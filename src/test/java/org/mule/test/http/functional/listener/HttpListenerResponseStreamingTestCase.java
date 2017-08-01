/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.apache.http.client.fluent.Request.Get;
import static org.apache.http.client.fluent.Request.Post;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.message.Message.builder;
import static org.mule.runtime.api.metadata.MediaType.TEXT;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;
import static org.mule.runtime.http.api.HttpHeaders.Values.MULTIPART_FORM_DATA;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.message.MultiPartPayload;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.api.message.DefaultMultiPartPayload;
import org.mule.runtime.core.api.message.PartAttributes;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Response;
import org.eclipse.jetty.util.MultiPartInputStreamParser;
import org.junit.Rule;
import io.qameta.allure.Feature;

@Feature(HTTP_EXTENSION)
public abstract class HttpListenerResponseStreamingTestCase extends AbstractHttpTestCase {

  private static final int DEFAULT_TIMEOUT = 5000;

  public static final String TEST_BODY = RandomStringUtils.randomAlphabetic(100 * 1024);
  public static final String TEST_BODY_MAP = "one=1&two=2";

  @Rule
  public SystemProperty stringPayloadLength = new SystemProperty("stringPayloadLength", String.valueOf(TEST_BODY.length()));
  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  protected abstract HttpVersion getHttpVersion();

  @Override
  protected String getConfigFile() {
    return "http-listener-response-streaming-config.xml";
  }

  protected String getUrl(String path) {
    return String.format("http://localhost:%s/%s", listenPort.getNumber(), path);
  }

  protected void testResponseIsContentLengthEncoding(String url, HttpVersion httpVersion) throws IOException {
    testResponseIsContentLengthEncoding(url, httpVersion, TEST_BODY);
  }

  protected void testResponseIsChunkedEncoding(String url, HttpVersion httpVersion) throws IOException {
    testResponseIsChunkedEncoding(url, httpVersion, TEST_BODY);
  }

  protected void testResponseIsNotChunkedEncoding(String url, HttpVersion httpVersion) throws IOException {
    testResponseIsNotChunkedEncoding(url, httpVersion, TEST_BODY);
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

  protected void testResponseIsMultipartContentLengthEncoding(String url, HttpVersion httpVersion) throws Exception {
    HttpResponse httpResponse = verifyIsContentLength(url, httpVersion);
    verifyMultipart(httpResponse);
  }

  protected void testResponseIsMultipartChunkedEncoding(String url, HttpVersion httpVersion) throws Exception {
    final Response response = Post(url).version(httpVersion).connectTimeout(DEFAULT_TIMEOUT).socketTimeout(DEFAULT_TIMEOUT)
        .execute();
    final HttpResponse httpResponse = response.returnResponse();
    verifyIsChunked(httpResponse);
    verifyMultipart(httpResponse);
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

  private void verifyMultipart(HttpResponse httpResponse) throws IOException, ServletException {
    String contentType = httpResponse.getFirstHeader(CONTENT_TYPE).getValue();
    assertThat(contentType, startsWith(MULTIPART_FORM_DATA));
    InputStream content = httpResponse.getEntity().getContent();
    MultiPartInputStreamParser partParser = new MultiPartInputStreamParser(content, contentType, null, null);
    Object[] parts = partParser.getParts().toArray();
    assertThat(parts, arrayWithSize(1));
    Part part = (Part) parts[0];
    assertThat(part.getContentType(), is(TEXT.toRfcString()));
    assertThat(part.getName(), is("aName"));
    assertThat(IOUtils.toString(part.getInputStream()), is(TEST_BODY));
  }

  private static MultiPartPayload createMultipartPayload() {
    PartAttributes partAttributes = new PartAttributes("aName");
    Message part = builder().value(TEST_BODY).attributesValue(partAttributes).mediaType(TEXT).build();
    return new DefaultMultiPartPayload(part);
  }

}
