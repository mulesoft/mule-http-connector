/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.http.client.fluent.Request.Post;
import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.MediaType.BINARY;
import static org.mule.runtime.api.metadata.MediaType.MULTIPART_MIXED;
import static org.mule.runtime.api.metadata.MediaType.TEXT;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_DISPOSITION;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;
import static org.mule.runtime.http.api.HttpHeaders.Values.MULTIPART_FORM_DATA;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.MULTIPART;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import io.qameta.allure.Story;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.util.MultiPartInputStreamParser;
import org.junit.Rule;
import org.junit.Test;

@Story(MULTIPART)
public class HttpListenerPartsTestCase extends AbstractHttpTestCase {

  private static final String TEXT_BODY_FIELD_NAME = "field1";
  private static final String TEXT_BODY_FIELD_VALUE = "yes";
  private static final String FILE_BODY_FIELD_NAME = "file";
  public static final String FILE_PART = randomAlphanumeric(1024) + " \n";
  // The value needs to be big enough to ensure several chunks if using transfer encoding chunked.
  private static final String FILE_BODY_FIELD_VALUE = StringUtils.repeat(FILE_PART, 16);
  private static final String FILE_BODY_FIELD_FILENAME = "file.ext";
  private static final MediaType TEXT_PLAIN_LATIN = MediaType.create("text", "plain", ISO_8859_1);
  private static final boolean DO_NOT_USE_CHUNKED_MODE = false;
  private static final boolean USE_CHUNKED_MODE = true;
  private static final String MIXED_CONTENT =
      "--the-boundary\r\n"
          + "Content-Type: text/plain; charset=ISO-8859-1\r\n"
          + "Content-Transfer-Encoding: 8bit\r\n"
          + "Content-Disposition: inline; name=\"field1\"\r\n"
          + "\r\n"
          + "yes\r\n"
          + "--the-boundary--\r\n";

  private static final String FILE1_FILENAME = "file1.txt";
  private static final String FILE2_FILENAME = "file2.txt";
  private static final String FILE1_VALUE = "first file";
  private static final String FILE2_VALUE = "second file";

  private static final String REPEATED_NAME =
      "--the-boundary\r\n"
          + "Content-Disposition: form-data; name=\"" + FILE_BODY_FIELD_NAME + "\"; filename=\"" + FILE1_FILENAME + "\"\r\n"
          + "Content-Type: text/plain\r\n"
          + "\r\n"
          + FILE1_VALUE + "\r\n"
          + "--the-boundary\r\n"
          + "Content-Disposition: form-data; name=\"" + FILE_BODY_FIELD_NAME + "\"; filename=\"" + FILE2_FILENAME + "\"\r\n"
          + "Content-Type: text/plain\r\n"
          + "\r\n"
          + FILE2_VALUE + "\r\n"
          + "--the-boundary--\r\n";

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");
  @Rule
  public SystemProperty formDataPath = new SystemProperty("formDataPath", "formDataPath");
  @Rule
  public SystemProperty mixedPath = new SystemProperty("mixedPath", "mixedPath");
  @Rule
  public SystemProperty contentLength = new SystemProperty("contentLength", "contentLength");
  @Rule
  public SystemProperty chunked = new SystemProperty("chunked", "chunked");
  @Rule
  public SystemProperty filePath = new SystemProperty("filePath", "filePath");
  @Rule
  public SystemProperty formDataChunkedPath = new SystemProperty("multipartChunked", "multipartChunked");
  @Rule
  public SystemProperty multipartResponse = new SystemProperty("multipartResponse", "multipartResponse");


  @Override
  protected String getConfigFile() {
    return "http-listener-attachment-config.xml";
  }

  @Test
  public void receiveOnlyAttachmentsAndReturnOnlyAttachments() throws Exception {
    processAttachmentRequestAndResponse(formDataPath.getValue(), MULTIPART_FORM_DATA, DO_NOT_USE_CHUNKED_MODE);
  }

  @Test
  public void receiveOnlyAttachmentsAndReturnOnlyAttachmentsWithMultipartMixedResponse() throws Exception {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(getUrl(mixedPath.getValue()));
      httpPost.setEntity(getMultipartEntity(false));
      try (final CloseableHttpResponse response = httpClient.execute(httpPost)) {
        final String contentType = response.getFirstHeader(CONTENT_TYPE).getValue();
        assertThat(contentType, containsString(MULTIPART_MIXED.toRfcString()));
        assertThat(IOUtils.toString(response.getEntity().getContent()), is(MIXED_CONTENT));
      }
    }
  }

  @Test
  public void receiveOnlyAttachmentsAndReturnOnlyAttachmentsWithMultipartFormDataAndTransferEncodingChunked() throws Exception {
    processAttachmentRequestAndResponse(formDataPath.getValue(), MULTIPART_FORM_DATA, USE_CHUNKED_MODE);
  }

  @Test
  public void respondWithAttachmentsContentLength() throws Exception {
    String contentLengthValue = getResponseWithExpectedAttachmentFrom(contentLength.getValue(), CONTENT_LENGTH);
    assertThat(contentLengthValue, is(notNullValue()));
  }

  @Test
  public void respondWithAttachmentsChunked() throws Exception {
    String transferEncodingValue = getResponseWithExpectedAttachmentFrom(chunked.getValue(), TRANSFER_ENCODING);
    assertThat(transferEncodingValue, is(CHUNKED));
  }

  @Test
  public void respondWithSeveralAttachments() throws Exception {
    final Response response = Post(getUrl(filePath.getValue()))
        .addHeader(CONTENT_TYPE, BINARY.toRfcString())
        .body(new StringEntity(FILE_BODY_FIELD_VALUE))
        .execute();
    HttpEntity entity = response.returnResponse().getEntity();
    Collection<Part> parts = parseParts(entity);

    assertThat(parts, hasSize(2));

    final Iterator<Part> responsePartsIterator = parts.iterator();

    Part part1 = responsePartsIterator.next();
    assertThat(part1.getName(), is(TEXT_BODY_FIELD_NAME));
    assertThat(part1.getContentType(), is(TEXT.toRfcString()));
    assertThat(part1.getHeader("Custom"), is("myHeader"));
    assertThat(IOUtils.toString(part1.getInputStream()), is(TEXT_BODY_FIELD_VALUE));

    Part part2 = responsePartsIterator.next();
    assertThat(part2.getName(), is(FILE_BODY_FIELD_NAME));
    assertThat(part2.getHeader(CONTENT_DISPOSITION), containsString("; filename=\"" + FILE_BODY_FIELD_FILENAME + "\""));
    assertThat(part2.getContentType(), is(BINARY.toRfcString()));
    assertThat(IOUtils.toString(part2.getInputStream()), is(FILE_BODY_FIELD_VALUE));
  }

  @Test
  public void respondWithSeveralAttachmentsWhenRepeated() throws Exception {
    final Response response = Post(getUrl(formDataPath.getValue()))
        .addHeader(CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=\"the-boundary\"")
        .body(new StringEntity(REPEATED_NAME))
        .execute();
    HttpEntity entity = response.returnResponse().getEntity();
    Collection<Part> parts = parseParts(entity);

    assertThat(parts, hasSize(2));

    final Iterator<Part> responsePartsIterator = parts.iterator();

    Part part1 = responsePartsIterator.next();
    assertThat(part1.getName(), is(FILE_BODY_FIELD_NAME));
    assertThat(part1.getHeader(CONTENT_DISPOSITION), containsString("; filename=\"" + FILE2_FILENAME + "\""));
    assertThat(part1.getContentType(), is(TEXT.toRfcString()));
    assertThat(IOUtils.toString(part1.getInputStream()), is(FILE2_VALUE));

    Part part2 = responsePartsIterator.next();
    assertThat(part2.getName(), is(FILE_BODY_FIELD_NAME));
    assertThat(part2.getHeader(CONTENT_DISPOSITION), containsString("; filename=\"" + FILE1_FILENAME + "\""));
    assertThat(part2.getContentType(), is(TEXT.toRfcString()));
    assertThat(IOUtils.toString(part2.getInputStream()), is(FILE1_VALUE));
  }

  private Collection<Part> parseParts(HttpEntity entity) throws IOException {
    MultiPartInputStreamParser inputStreamParser = new MultiPartInputStreamParser(entity.getContent(),
                                                                                  entity.getContentType().getValue(),
                                                                                  null, null);
    return inputStreamParser.getParts();
  }

  private String getResponseWithExpectedAttachmentFrom(String path, String requiredHeader) throws MuleException, IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(getUrl(path));
      httpPost.setEntity(new StringEntity(FILE_BODY_FIELD_VALUE));
      httpPost.setHeader(CONTENT_TYPE, BINARY.toRfcString());
      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        final String contentType = response.getFirstHeader(CONTENT_TYPE).getValue();
        assertThat(contentType, containsString(MULTIPART_FORM_DATA));

        final Collection<Part> parts = parseParts(response.getEntity());
        assertThat(parts.size(), is(1));
        Part part = parts.iterator().next();
        assertThat(part.getName(), is(FILE_BODY_FIELD_NAME));
        assertThat(part.getHeader(CONTENT_DISPOSITION), containsString("; filename=\"" + FILE_BODY_FIELD_FILENAME + "\""));
        assertThat(IOUtils.toString(part.getInputStream()), is(FILE_BODY_FIELD_VALUE));
        return response.getFirstHeader(requiredHeader).getValue();
      }
    }
  }

  private void processAttachmentRequestAndResponse(String pathToCall, String expectedResponseContentType, boolean useChunkedMode)
      throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(getUrl(pathToCall));
      HttpEntity multipart = createHttpEntity(useChunkedMode);
      httpPost.setEntity(multipart);
      try (final CloseableHttpResponse response = httpClient.execute(httpPost)) {
        final String contentType = response.getFirstHeader(CONTENT_TYPE).getValue();
        assertThat(contentType, containsString(expectedResponseContentType));

        final Collection<Part> parts = parseParts(response.getEntity());
        assertThat(parts.size(), is(2));

        final Iterator<Part> responsePartsIterator = parts.iterator();

        Part part1 = responsePartsIterator.next();
        assertThat(part1.getName(), is(TEXT_BODY_FIELD_NAME));
        assertThat(part1.getContentType(), is(TEXT_PLAIN_LATIN.toRfcString()));
        assertThat(IOUtils.toString(part1.getInputStream()), is(TEXT_BODY_FIELD_VALUE));

        Part part2 = responsePartsIterator.next();
        assertThat(part2.getName(), is(FILE_BODY_FIELD_NAME));
        assertThat(part2.getContentType(), is(BINARY.toRfcString()));
        assertThat(IOUtils.toString(part2.getInputStream()), is(FILE_BODY_FIELD_VALUE));
      }
    }
  }

  private HttpEntity createHttpEntity(boolean useChunkedMode) throws IOException {
    HttpEntity multipartEntity = getMultipartEntity(true);
    if (useChunkedMode) {
      // The only way to send multipart + chunked is putting the multipart content in an output stream entity.
      ByteArrayOutputStream multipartOutput = new ByteArrayOutputStream();
      multipartEntity.writeTo(multipartOutput);
      multipartOutput.flush();
      ByteArrayEntity byteArrayEntity = new ByteArrayEntity(multipartOutput.toByteArray());
      multipartOutput.close();

      byteArrayEntity.setChunked(true);
      byteArrayEntity.setContentEncoding(multipartEntity.getContentEncoding());
      byteArrayEntity.setContentType(multipartEntity.getContentType());
      return byteArrayEntity;
    } else {
      return multipartEntity;
    }
  }

  private HttpEntity getMultipartEntity(boolean withFile) {
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    if (withFile) {
      builder.addBinaryBody(FILE_BODY_FIELD_NAME, FILE_BODY_FIELD_VALUE.getBytes(), APPLICATION_OCTET_STREAM,
                            FILE_BODY_FIELD_FILENAME);
    }
    builder.addTextBody(TEXT_BODY_FIELD_NAME, TEXT_BODY_FIELD_VALUE, TEXT_PLAIN);
    return builder.build();
  }

  private String getUrl(String pathToCall) {
    return format("http://localhost:%s/%s", listenPort.getNumber(), pathToCall);
  }

}
