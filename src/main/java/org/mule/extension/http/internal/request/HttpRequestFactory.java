/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.String.valueOf;
import static org.mule.extension.http.api.error.HttpError.SECURITY;
import static org.mule.extension.http.api.error.HttpError.TRANSFORMATION;
import static org.mule.extension.http.internal.HttpStreamingType.ALWAYS;
import static org.mule.extension.http.internal.HttpStreamingType.AUTO;
import static org.mule.extension.http.internal.HttpStreamingType.NEVER;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.api.metadata.DataType.BYTE_ARRAY;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.COOKIE;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.internal.HttpStreamingType;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that generates {@link HttpRequest HttpRequests}.
 *
 * @since 1.0
 */
public class HttpRequestFactory {

  private static final Logger logger = LoggerFactory.getLogger(HttpRequestFactory.class);
  private static final List<String> DEFAULT_EMPTY_BODY_METHODS = Lists.newArrayList("GET", "HEAD", "OPTIONS");
  private static final String BOTH_TRANSFER_HEADERS_SET_MESSAGE =
      "Cannot send both Transfer-Encoding and Content-Length headers. Transfer-Encoding will not be sent.";
  private static final String TRANSFER_ENCODING_NOT_ALLOWED_WHEN_NEVER_MESSAGE =
      "Transfer-Encoding header will not be sent, as the configured requestStreamingMode is NEVER.";
  private static final String INVALID_TRANSFER_ENCODING_HEADER_MESSAGE =
      "Transfer-Encoding header value was invalid and will not be sent.";

  private final String uri;
  private final String method;
  private final HttpRequesterCookieConfig config;
  private final HttpStreamingType streamingMode;
  private final HttpSendBodyMode sendBodyMode;
  private final TransformationService transformationService;


  public HttpRequestFactory(HttpRequesterCookieConfig config, String uri, String method, HttpStreamingType streamingMode,
                            HttpSendBodyMode sendBodyMode, TransformationService transformationService) {
    this.config = config;
    this.uri = uri;
    this.method = method;
    this.streamingMode = streamingMode;
    this.sendBodyMode = sendBodyMode;
    this.transformationService = transformationService;
  }

  /**
   * Creates an {@HttpRequest}.
   *
   * @param requestBuilder The generic {@link HttpRequesterRequestBuilder} from the request component that should be used to
   *        create the {@link HttpRequest}.
   * @param authentication The {@link HttpRequestAuthentication} that should be used to create the {@link HttpRequest}.
   * @return an {@HttpRequest} configured based on the parameters.
   * @throws MuleException if the request creation fails.
   */
  public HttpRequest create(HttpRequesterRequestBuilder requestBuilder, HttpRequestAuthentication authentication) {
    HttpRequestBuilder builder = HttpRequest.builder();

    builder.uri(this.uri)
        .method(this.method)
        .headers(requestBuilder.getHeaders())
        .queryParams(requestBuilder.getQueryParams());

    MediaType mediaType = requestBuilder.getBody().getDataType().getMediaType();
    if (!builder.getHeaderValue(CONTENT_TYPE).isPresent()) {
      if (!MediaType.ANY.matches(mediaType)) {
        builder.addHeader(CONTENT_TYPE, mediaType.toRfcString());
      }
    }

    if (config.isEnableCookies()) {
      try {
        Map<String, List<String>> headers =
            config.getCookieManager().get(builder.getUri(), Collections.<String, List<String>>emptyMap());
        List<String> cookies = headers.get(COOKIE);
        if (cookies != null) {
          for (String cookie : cookies) {
            builder.addHeader(COOKIE, cookie);
          }
        }
      } catch (IOException e) {
        logger.warn("Error reading cookies for URI " + uri, e);
      }

    }

    try {
      builder.entity(createRequestEntity(builder, this.method, requestBuilder.getBody()));
    } catch (Exception e) {
      throw new ModuleException(TRANSFORMATION, e);
    }

    if (authentication != null) {
      try {
        authentication.authenticate(builder);
      } catch (MuleException e) {
        throw new ModuleException(SECURITY, e);
      }
    }

    return builder.build();
  }

  private HttpEntity createRequestEntity(HttpRequestBuilder requestBuilder, String resolvedMethod, TypedValue<?> body) {
    HttpEntity entity;

    Object payload = body.getValue();
    Optional<Long> length = body.getLength();
    Optional<String> transferEncoding = requestBuilder.getHeaderValue(TRANSFER_ENCODING);
    Optional<String> contentLength = requestBuilder.getHeaderValue(CONTENT_LENGTH);

    if (isEmptyBody(payload, resolvedMethod)) {
      entity = new EmptyHttpEntity();
    } else if (payload instanceof InputStream || payload instanceof CursorStreamProvider) {
      payload = payload instanceof CursorStreamProvider ? ((CursorStreamProvider) payload).openCursor() : payload;
      if (streamingMode == ALWAYS) {
        entity = guaranteeStreaming(requestBuilder, transferEncoding, contentLength, (InputStream) payload);
      } else if (streamingMode == AUTO) {
        if (contentLength.isPresent()) {
          sanitizeForContentLength(requestBuilder, transferEncoding, BOTH_TRANSFER_HEADERS_SET_MESSAGE);
          entity = new ByteArrayHttpEntity(getPayloadAsBytes(payload));
        } else if ((transferEncoding.isPresent() && CHUNKED.equalsIgnoreCase(transferEncoding.get())) || !length.isPresent()) {
          entity = new InputStreamHttpEntity((InputStream) payload);
        } else {
          sanitizeForContentLength(requestBuilder, transferEncoding, INVALID_TRANSFER_ENCODING_HEADER_MESSAGE);
          entity = avoidConsumingPayload(requestBuilder, (InputStream) payload, length);
        }
      } else {
        sanitizeForContentLength(requestBuilder, transferEncoding, TRANSFER_ENCODING_NOT_ALLOWED_WHEN_NEVER_MESSAGE);
        if (length.isPresent()) {
          entity = avoidConsumingPayload(requestBuilder, (InputStream) payload, length);
        } else {
          entity = new ByteArrayHttpEntity(getPayloadAsBytes(payload));
        }
      }
    } else {
      byte[] payloadAsBytes = getPayloadAsBytes(payload);
      if (streamingMode == ALWAYS) {
        entity = guaranteeStreaming(requestBuilder, transferEncoding, contentLength, new ByteArrayInputStream(payloadAsBytes));
      } else if (streamingMode == NEVER) {
        sanitizeForContentLength(requestBuilder, transferEncoding, TRANSFER_ENCODING_NOT_ALLOWED_WHEN_NEVER_MESSAGE);
        entity = new ByteArrayHttpEntity(payloadAsBytes);
      } else {
        // AUTO is defined so we'll let the headers define the transfer type
        if (contentLength.isPresent() && transferEncoding.isPresent()) {
          sanitizeForContentLength(requestBuilder, transferEncoding, BOTH_TRANSFER_HEADERS_SET_MESSAGE);
        }
        entity = new InputStreamHttpEntity(new ByteArrayInputStream(payloadAsBytes), (long) payloadAsBytes.length);
      }
    }

    return entity;
  }

  private boolean isEmptyBody(Object body, String method) {
    boolean emptyBody;

    if (body == null) {
      emptyBody = true;
    } else {
      emptyBody = DEFAULT_EMPTY_BODY_METHODS.contains(method);

      if (sendBodyMode != HttpSendBodyMode.AUTO) {
        emptyBody = (sendBodyMode == HttpSendBodyMode.NEVER);
      }
    }

    return emptyBody;
  }

  /**
   * Generates an {@link InputStreamHttpEntity} with no length and sanitizes the headers for chunking
   */
  private HttpEntity guaranteeStreaming(HttpRequestBuilder requestBuilder, Optional<String> transferEncoding,
                                        Optional<String> contentLength, InputStream stream) {
    sanitizeForStreaming(requestBuilder, transferEncoding, contentLength);
    return new InputStreamHttpEntity(stream);
  }

  /**
   * Generates an {@link InputStreamHttpEntity} with a length and sets the Content-Length header with it as well
   */
  private HttpEntity avoidConsumingPayload(HttpRequestBuilder requestBuilder, InputStream payload, Optional<Long> length) {
    requestBuilder.addHeader(CONTENT_LENGTH, valueOf(length.get()));
    return new InputStreamHttpEntity(payload, length.get());
  }

  private byte[] getPayloadAsBytes(Object payload) {
    return (byte[]) transformationService.transform(of(payload), BYTE_ARRAY).getPayload().getValue();
  }

  private void sanitizeForStreaming(HttpRequestBuilder requestBuilder, Optional<String> transferEncodingHeader,
                                    Optional<String> contentLengthHeader) {
    if (contentLengthHeader != null) {
      requestBuilder.removeHeader(CONTENT_LENGTH);

      if (logger.isDebugEnabled()) {
        logger.debug("Content-Length header will not be sent, as the configured requestStreamingMode is ALWAYS.");
      }
    }

    if (transferEncodingHeader.isPresent() && !transferEncodingHeader.get().equalsIgnoreCase(CHUNKED)) {
      requestBuilder.removeHeader(TRANSFER_ENCODING);

      if (logger.isDebugEnabled()) {
        logger.debug("Transfer-Encoding header will be sent with value 'chunked' instead of {}, as the configured "
            + "requestStreamingMode is ALWAYS", transferEncodingHeader);
      }

    }
  }

  private void sanitizeForContentLength(HttpRequestBuilder requestBuilder, Optional<String> transferEncoding, String reason) {
    if (transferEncoding.isPresent()) {
      requestBuilder.removeHeader(TRANSFER_ENCODING);

      if (logger.isDebugEnabled()) {
        logger.debug(reason);
      }
    }
  }
}
