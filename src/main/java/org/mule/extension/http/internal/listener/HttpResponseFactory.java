/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.http.internal.listener;

import static org.mule.extension.http.internal.HttpStreamingType.ALWAYS;
import static org.mule.extension.http.internal.HttpStreamingType.AUTO;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.api.metadata.DataType.BYTE_ARRAY;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_MODIFIED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NO_CONTENT;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.getReasonPhraseForStatusCode;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;

import org.mule.extension.http.api.listener.builder.HttpListenerResponseBuilder;
import org.mule.extension.http.internal.HttpStreamingType;
import org.mule.extension.http.internal.listener.intercepting.Interception;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.exception.MessagingException;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.http.api.domain.message.response.HttpResponseBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that creates {@link HttpResponse HttpResponses}.
 *
 * @since 1.0
 */
public class HttpResponseFactory {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private HttpStreamingType responseStreaming = AUTO;
  private TransformationService transformationService;

  public HttpResponseFactory(HttpStreamingType responseStreaming,
                             TransformationService transformationService) {
    this.responseStreaming = responseStreaming;
    this.transformationService = transformationService;
  }

  /**
   * Creates an {@HttpResponse}.
   *
   * @param responseBuilder the {@link HttpResponseBuilder} that should be modified if necessary and used to build the
   *        {@link HttpResponse}.
   * @param interception the {@link Interception} that should be taken into account when building the {@link HttpResponse}.
   * @param listenerResponseBuilder the generic {@HttpListenerResponseBuilder} configured for this listener.
   * @param supportsTransferEncoding boolean that determines whether the HTTP protocol of the response supports streaming.
   * @return an {@HttpResponse} configured based on the parameters.
   * @throws MessagingException if the response creation fails.
   */
  public HttpResponse create(HttpResponseBuilder responseBuilder,
                             Interception interception,
                             HttpListenerResponseBuilder listenerResponseBuilder,
                             boolean supportsTransferEncoding)
      throws IOException {

    final HttpResponseHeaderBuilder httpResponseHeaderBuilder = new HttpResponseHeaderBuilder();

    addInterceptingHeaders(interception, httpResponseHeaderBuilder);
    addUserHeaders(listenerResponseBuilder, supportsTransferEncoding, httpResponseHeaderBuilder);

    TypedValue<Object> body = listenerResponseBuilder.getBody();
    if (httpResponseHeaderBuilder.getContentType() == null && !ANY.matches(body.getDataType().getMediaType())) {
      httpResponseHeaderBuilder.addHeader(CONTENT_TYPE, body.getDataType().getMediaType().toRfcString());
    }

    final String existingTransferEncoding = httpResponseHeaderBuilder.getTransferEncoding();
    final String existingContentLength = httpResponseHeaderBuilder.getContentLength();

    HttpEntity httpEntity;
    Object payload = body.getValue();

    if (payload == null) {
      setupContentLengthEncoding(httpResponseHeaderBuilder, 0);
      httpEntity = new EmptyHttpEntity();
    } else if (payload instanceof InputStream) {
      if (responseStreaming == ALWAYS
          || (responseStreaming == AUTO && existingContentLength == null)) {
        if (supportsTransferEncoding) {
          setupChunkedEncoding(httpResponseHeaderBuilder);
        }
        httpEntity = new InputStreamHttpEntity((InputStream) payload);
      } else {
        ByteArrayHttpEntity byteArrayHttpEntity = new ByteArrayHttpEntity(IOUtils.toByteArray(((InputStream) payload)));
        setupContentLengthEncoding(httpResponseHeaderBuilder, byteArrayHttpEntity.getBytes().length);
        httpEntity = byteArrayHttpEntity;
      }
    } else {
      ByteArrayHttpEntity byteArrayHttpEntity = new ByteArrayHttpEntity(getMessageAsBytes(payload));

      resolveEncoding(httpResponseHeaderBuilder, existingTransferEncoding, existingContentLength, supportsTransferEncoding,
                      byteArrayHttpEntity);
      httpEntity = byteArrayHttpEntity;
    }

    Integer statusCode = listenerResponseBuilder.getStatusCode();
    if (statusCode != null) {
      responseBuilder.statusCode(statusCode);
      if (statusCode == NO_CONTENT.getStatusCode() || statusCode == NOT_MODIFIED.getStatusCode()) {
        httpEntity = new EmptyHttpEntity();
        httpResponseHeaderBuilder.removeHeader(TRANSFER_ENCODING);
      }
    }
    String reasonPhrase = resolveReasonPhrase(listenerResponseBuilder.getReasonPhrase(), statusCode);
    if (reasonPhrase != null) {
      responseBuilder.reasonPhrase(reasonPhrase);
    }

    Collection<String> headerNames = httpResponseHeaderBuilder.getHeaderNames();
    for (String headerName : headerNames) {
      Collection<String> values = httpResponseHeaderBuilder.getHeader(headerName);
      for (String value : values) {
        responseBuilder.addHeader(headerName, value);
      }
    }

    responseBuilder.entity(httpEntity);
    return responseBuilder.build();
  }

  private void addInterceptingHeaders(Interception interception, HttpResponseHeaderBuilder httpResponseHeaderBuilder) {
    interception
        .getHeaders().keySet()
        .forEach(headerKey -> httpResponseHeaderBuilder.addHeader(headerKey, interception.getHeaders().get(headerKey)));
  }

  private void addUserHeaders(HttpListenerResponseBuilder listenerResponseBuilder, boolean supportsTransferEncoding,
                              HttpResponseHeaderBuilder httpResponseHeaderBuilder) {
    MultiMap<String, String> headers = listenerResponseBuilder.getHeaders();

    headers.entryList().forEach(entry -> {
      if (TRANSFER_ENCODING.equals(entry.getKey()) && !supportsTransferEncoding) {
        logger.debug(
                     "Client HTTP version is lower than 1.1 so the unsupported 'Transfer-Encoding' header has been removed and 'Content-Length' will be sent instead.");
      } else {
        httpResponseHeaderBuilder.addHeader(entry.getKey(), entry.getValue());
      }
    });
  }

  private byte[] getMessageAsBytes(Object payload) {
    return (byte[]) transformationService.transform(of(payload), BYTE_ARRAY).getPayload().getValue();
  }

  public String resolveReasonPhrase(String builderReasonPhrase, Integer statusCode) {
    String reasonPhrase = builderReasonPhrase;
    if (reasonPhrase == null && statusCode != null) {
      reasonPhrase = getReasonPhraseForStatusCode(statusCode);
    }
    return reasonPhrase;
  }

  private void resolveEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder, String existingTransferEncoding,
                               String existingContentLength, boolean supportsTransferEncoding,
                               ByteArrayHttpEntity byteArrayHttpEntity) {
    if (responseStreaming == ALWAYS
        || (responseStreaming == AUTO && existingContentLength == null
            && CHUNKED.equals(existingTransferEncoding))) {
      if (supportsTransferEncoding) {
        setupChunkedEncoding(httpResponseHeaderBuilder);
      }
    } else {
      setupContentLengthEncoding(httpResponseHeaderBuilder, byteArrayHttpEntity.getBytes().length);
    }
  }

  private void setupContentLengthEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder, int contentLength) {
    if (httpResponseHeaderBuilder.getTransferEncoding() != null) {
      logger.debug("Content-Length encoding is being used so the 'Transfer-Encoding' header has been removed");
      httpResponseHeaderBuilder.removeHeader(TRANSFER_ENCODING);
    }
    httpResponseHeaderBuilder.setContentLength(String.valueOf(contentLength));
  }

  private void setupChunkedEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder) {
    if (httpResponseHeaderBuilder.getContentLength() != null) {
      logger.debug("Chunked encoding is being used so the 'Content-Length' header has been removed");
      httpResponseHeaderBuilder.removeHeader(CONTENT_LENGTH);
    }
    String existingTransferEncoding = httpResponseHeaderBuilder.getTransferEncoding();
    if (!CHUNKED.equals(existingTransferEncoding)) {
      httpResponseHeaderBuilder.addHeader(TRANSFER_ENCODING, CHUNKED);
    }
  }

}
