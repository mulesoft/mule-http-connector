/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.http.internal.listener;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.mule.extension.http.api.streaming.HttpStreamingType.ALWAYS;
import static org.mule.extension.http.api.streaming.HttpStreamingType.AUTO;
import static org.mule.runtime.api.metadata.DataType.BYTE_ARRAY;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_MODIFIED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NO_CONTENT;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.getReasonPhraseForStatusCode;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;

import org.mule.runtime.api.streaming.object.CursorIteratorProvider;
import org.mule.extension.http.api.listener.builder.HttpListenerResponseBuilder;
import org.mule.extension.http.api.streaming.HttpStreamingType;
import org.mule.extension.http.internal.listener.intercepting.Interception;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.http.api.domain.message.response.HttpResponseBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that creates {@link HttpResponse HttpResponses}.
 *
 * @since 1.0
 */
public class HttpResponseFactory {

  private static final String HEADER_TRANSFER_ENCODING = TRANSFER_ENCODING.toLowerCase();

  private Logger logger = LoggerFactory.getLogger(getClass());

  private HttpStreamingType responseStreaming = AUTO;
  private TransformationService transformationService;
  private Map<Class, TriFunction<TypedValue, Boolean, HttpResponseHeaderBuilder, HttpEntity>> payloadHandlerMapper;

  private static final String INVALID_DATA_MSG = "Attempted to send invalid data through http response.";

  public HttpResponseFactory(HttpStreamingType responseStreaming,
                             TransformationService transformationService) {
    this.responseStreaming = responseStreaming;
    this.transformationService = transformationService;
    this.payloadHandlerMapper = new HashMap<>();
    initResponsePayloadHandlers();
  }

  @FunctionalInterface
  interface TriFunction<A, B, C, R> {

    R apply(A a, B b, C c);
  }

  private void initResponsePayloadHandlers() {
    payloadHandlerMapper.put(CursorStreamProvider.class, this::handleCursorStreamProvider);
    payloadHandlerMapper.put(InputStream.class, this::handlePayload);
    payloadHandlerMapper.put(CursorIteratorProvider.class, this::handleInvalidType);
  }

  private Optional<TriFunction<TypedValue, Boolean, HttpResponseHeaderBuilder, HttpEntity>> getHandler(Class key) {
    for (Class classKey : this.payloadHandlerMapper.keySet()) {
      if (classKey.isAssignableFrom(key)) {
        return ofNullable(this.payloadHandlerMapper.get(classKey));
      }
    }
    return empty();
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
   * @throws IOException if the response creation fails.
   */
  public HttpResponse create(HttpResponseBuilder responseBuilder,
                             Interception interception,
                             HttpListenerResponseBuilder listenerResponseBuilder,
                             boolean supportsTransferEncoding) {

    final HttpResponseHeaderBuilder httpResponseHeaderBuilder = new HttpResponseHeaderBuilder(responseBuilder);

    addInterceptingHeaders(interception, httpResponseHeaderBuilder);
    addUserHeaders(listenerResponseBuilder, supportsTransferEncoding, httpResponseHeaderBuilder);

    TypedValue<Object> body = listenerResponseBuilder.getBody();
    if (httpResponseHeaderBuilder.getContentType() == null && !ANY.matches(body.getDataType().getMediaType())) {
      httpResponseHeaderBuilder.addHeader(CONTENT_TYPE, body.getDataType().getMediaType().toRfcString());
    }

    HttpEntity httpEntity;
    Object payload = body.getValue();

    if (payload == null) {
      setupContentLengthEncoding(httpResponseHeaderBuilder, 0);
      httpEntity = new EmptyHttpEntity();
    } else {
      httpEntity = getHandler(payload.getClass())
          .map(payloadHandler -> payloadHandler.apply(body, supportsTransferEncoding, httpResponseHeaderBuilder))
          .orElseGet(() -> {
            ByteArrayHttpEntity byteArrayHttpEntity = new ByteArrayHttpEntity(getMessageAsBytes(body));

            resolveEncoding(httpResponseHeaderBuilder, supportsTransferEncoding, byteArrayHttpEntity);
            return byteArrayHttpEntity;
          });
    }

    Integer statusCode = listenerResponseBuilder.getStatusCode();
    if (statusCode != null) {
      if (statusCode < 200) {
        logger.warn(format("Response status code '%s' cannot be sent as a final response since it's lower than 200.",
                           statusCode));
        throw new IllegalArgumentException("Cannot send a status code lower than 200");
      } else {
        responseBuilder.statusCode(statusCode);
        if (statusCode == NO_CONTENT.getStatusCode() || statusCode == NOT_MODIFIED.getStatusCode()) {
          httpEntity = new EmptyHttpEntity();
          httpResponseHeaderBuilder.removeHeader(HEADER_TRANSFER_ENCODING);
        }
      }
    }
    String reasonPhrase = resolveReasonPhrase(listenerResponseBuilder.getReasonPhrase(), statusCode);
    if (reasonPhrase != null) {
      responseBuilder.reasonPhrase(reasonPhrase);
    }

    responseBuilder.entity(httpEntity);
    return responseBuilder.build();
  }

  private void addInterceptingHeaders(Interception interception, HttpResponseHeaderBuilder httpResponseHeaderBuilder) {
    MultiMap<String, String> headers = interception.getHeaders();
    headers.keySet().forEach(key -> httpResponseHeaderBuilder.addHeader(key, headers.getAll(key)));
  }

  private void addUserHeaders(HttpListenerResponseBuilder listenerResponseBuilder, boolean supportsTransferEncoding,
                              HttpResponseHeaderBuilder httpResponseHeaderBuilder) {
    MultiMap<String, String> headers = listenerResponseBuilder.getHeaders();

    headers.keySet().forEach(key -> {
      if (!supportsTransferEncoding && HEADER_TRANSFER_ENCODING.equalsIgnoreCase(key)) {
        logger.debug(
                     "Client HTTP version is lower than 1.1 so the unsupported 'Transfer-Encoding' header has been removed and 'Content-Length' will be sent instead.");
      } else {
        httpResponseHeaderBuilder.addHeader(key, headers.getAll(key));
      }
    });
  }

  private byte[] getMessageAsBytes(TypedValue payload) {
    return (byte[]) transformationService.transform(Message.builder().payload(payload).build(), BYTE_ARRAY).getPayload()
        .getValue();
  }

  public String resolveReasonPhrase(String builderReasonPhrase, Integer statusCode) {
    String reasonPhrase = builderReasonPhrase;
    if (reasonPhrase == null && statusCode != null) {
      reasonPhrase = getReasonPhraseForStatusCode(statusCode);
    }
    return reasonPhrase;
  }
  

  /**
   * Generates an {@link InputStreamHttpEntity} without length and makes chunking explicit if supported
   */
  private HttpEntity guaranteeStreamingIfPossible(boolean possible, HttpResponseHeaderBuilder headerBuilder, Object stream) {
    if (possible) {
      setupChunkedEncoding(headerBuilder, CHUNKED.equals(headerBuilder.getTransferEncoding()));
    }
    return new InputStreamHttpEntity((InputStream) stream);
  }

  /**
   * Generates an {@link InputStreamHttpEntity} with the body's length and the content length explicit with it
   */
  private HttpEntity avoidConsumingPayload(HttpResponseHeaderBuilder httpResponseHeaderBuilder, Object payload, Long length) {
    setupContentLengthEncoding(httpResponseHeaderBuilder, length);
    return new InputStreamHttpEntity((InputStream) payload, length);
  }

  /**
   * Generates a {@link ByteArrayHttpEntity} and makes the content length explicit with it
   */
  private HttpEntity consumePayload(HttpResponseHeaderBuilder httpResponseHeaderBuilder, TypedValue stream) {
    ByteArrayHttpEntity byteArrayHttpEntity = new ByteArrayHttpEntity(getMessageAsBytes(stream));
    setupContentLengthEncoding(httpResponseHeaderBuilder, byteArrayHttpEntity.getBytes().length);
    return byteArrayHttpEntity;
  }

  private void resolveEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder, boolean supportsTransferEncoding,
                               ByteArrayHttpEntity byteArrayHttpEntity) {
    boolean chunkedTransferEncoding = CHUNKED.equals(httpResponseHeaderBuilder.getTransferEncoding());
    if (responseStreaming == ALWAYS
        || (chunkedTransferEncoding && responseStreaming == AUTO &&
            httpResponseHeaderBuilder.getContentLength() == null)) {
      if (supportsTransferEncoding) {
        setupChunkedEncoding(httpResponseHeaderBuilder, chunkedTransferEncoding);
      }
    } else {
      setupContentLengthEncoding(httpResponseHeaderBuilder, byteArrayHttpEntity.getBytes().length);
    }
  }

  private void setupContentLengthEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder, long contentLength) {
    if (httpResponseHeaderBuilder.getTransferEncoding() != null) {
      logger.debug("Content-Length encoding is being used so the 'Transfer-Encoding' header has been removed");
      httpResponseHeaderBuilder.removeHeader(HEADER_TRANSFER_ENCODING);
    }
    httpResponseHeaderBuilder.setContentLength(String.valueOf(contentLength));
  }

  private void setupChunkedEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder, boolean chunkedTransferEncoding) {
    if (httpResponseHeaderBuilder.getContentLength() != null) {
      logger.debug("Chunked encoding is being used so the 'Content-Length' header has been removed");
      httpResponseHeaderBuilder.removeHeader(CONTENT_LENGTH);
    }
    if (!chunkedTransferEncoding) {
      httpResponseHeaderBuilder.addHeader(HEADER_TRANSFER_ENCODING, CHUNKED);
    }
  }

  public HttpEntity handleCursorStreamProvider(TypedValue body, Boolean supportsTransferEncoding,
                                               HttpResponseHeaderBuilder responseHeaderBuilder) {
    Object payload = ((CursorStreamProvider) body.getValue()).openCursor();
    return handleStreamProvider(responseHeaderBuilder, body, payload, supportsTransferEncoding);
  }

  public HttpEntity handlePayload(TypedValue body, Boolean supportsTransferEncoding,
                                  HttpResponseHeaderBuilder responseHeaderBuilder) {
    return handleStreamProvider(responseHeaderBuilder, body, body.getValue(), supportsTransferEncoding);
  }

  public HttpEntity handleInvalidType(TypedValue body, Boolean supportsTransferEncoding,
                                      HttpResponseHeaderBuilder responseHeaderBuilder) {
    throw new RuntimeException(INVALID_DATA_MSG);
  }

  public HttpEntity handleStreamProvider(HttpResponseHeaderBuilder headerBuilder, TypedValue body, Object payload,
                                         boolean supportsTransferEncoding) {
    HttpEntity httpEntity;
    final boolean hasLength = body.getLength().isPresent();

    final String existingTransferEncoding = headerBuilder.getTransferEncoding();
    final String existingContentLength = headerBuilder.getContentLength();

    if (responseStreaming == ALWAYS) {
      httpEntity = guaranteeStreamingIfPossible(supportsTransferEncoding, headerBuilder, payload);
    } else if (responseStreaming == AUTO) {
      if (existingContentLength != null) {
        // We can't guarantee the length is right, but we know that was desired
        httpEntity = consumePayload(headerBuilder, body);
      } else if (CHUNKED.equals(existingTransferEncoding) || !hasLength) {
        // Either chunking was explicit or we have no choice
        httpEntity = guaranteeStreamingIfPossible(supportsTransferEncoding, headerBuilder, payload);
      } else {
        // No explicit desire but we have a length to take advantage of
        httpEntity = avoidConsumingPayload(headerBuilder, payload, body.getByteLength().getAsLong());
      }
    } else {
      // NEVER was selected but we could take advantage of the length
      if (hasLength) {
        httpEntity = avoidConsumingPayload(headerBuilder, payload, body.getByteLength().getAsLong());
      } else {
        httpEntity = consumePayload(headerBuilder, body);
      }
    }
    return httpEntity;
  }

}
