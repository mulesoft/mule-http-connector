/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyMap;
import static org.mule.extension.http.api.error.HttpError.SECURITY;
import static org.mule.extension.http.api.error.HttpError.TRANSFORMATION;
import static org.mule.extension.http.api.streaming.HttpStreamingType.ALWAYS;
import static org.mule.extension.http.api.streaming.HttpStreamingType.AUTO;
import static org.mule.extension.http.api.streaming.HttpStreamingType.NEVER;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.api.metadata.DataType.BYTE_ARRAY;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.COOKIE;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Names.X_CORRELATION_ID;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.streaming.HttpStreamingType;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Component that generates {@link HttpRequest HttpRequests}.
 *
 * @since 1.0
 */
public class HttpRequestFactory {


  private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestFactory.class);

  private static final String CONTENT_TYPE_HEADER = CONTENT_TYPE.toLowerCase();
  private static final String CONTENT_LENGTH_HEADER = CONTENT_LENGTH.toLowerCase();
  private static final String TRANSFER_ENCODING_HEADER = TRANSFER_ENCODING.toLowerCase();

  private static final Set<String> DEFAULT_EMPTY_BODY_METHODS = newHashSet("GET", "HEAD", "OPTIONS");

  private static final String BOTH_TRANSFER_HEADERS_SET_MESSAGE =
      "Cannot send both Transfer-Encoding and Content-Length headers. Transfer-Encoding will not be sent.";
  private static final String TRANSFER_ENCODING_NOT_ALLOWED_WHEN_NEVER_MESSAGE =
      "Transfer-Encoding header will not be sent, as the configured requestStreamingMode is NEVER.";
  private static final String INVALID_TRANSFER_ENCODING_HEADER_MESSAGE =
      "Transfer-Encoding header value was invalid and will not be sent.";

  /**
   * Creates an {@HttpRequest}.
   *
   * @param requestBuilder The generic {@link HttpRequesterRequestBuilder} from the request component that should be used to
   *                       create the {@link HttpRequest}.
   * @param authentication The {@link HttpRequestAuthentication} that should be used to create the {@link HttpRequest}.
   * @return an {@HttpRequest} configured based on the parameters.
   * @throws MuleException if the request creation fails.
   */
  public HttpRequest create(HttpRequesterConfig config, String uri, String method, HttpStreamingType streamingMode,
                            HttpSendBodyMode sendBodyMode, TransformationService transformationService,
                            HttpRequesterRequestBuilder requestBuilder, HttpRequestAuthentication authentication) {
    HttpRequestBuilder builder = HttpRequest.builder(PRESERVE_HEADER_CASE || config.isPreserveHeadersCase());

    builder.uri(uri)
        .method(method)
        .headers(requestBuilder.getHeaders())
        .queryParams(requestBuilder.getQueryParams());

    config.getDefaultHeaders()
        .forEach(header -> builder.addHeader(header.getKey(), header.getValue()));

    config.getDefaultQueryParams()
        .forEach(param -> builder.addQueryParam(param.getKey(), param.getValue()));

    MediaType mediaType = requestBuilder.getBody().getDataType().getMediaType();
    if (!builder.getHeaderValue(CONTENT_TYPE_HEADER).isPresent()) {
      if (!MediaType.ANY.matches(mediaType)) {
        builder.addHeader(CONTENT_TYPE_HEADER, mediaType.toRfcString());
      }
    }

    requestBuilder.getSendCorrelationId()
        .getOutboundCorrelationId(requestBuilder.getCorrelationInfo(), requestBuilder.getCorrelationId())
        .ifPresent(correlationId -> {
          if (builder.getHeaderValue(X_CORRELATION_ID).isPresent()) {
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug(
                           X_CORRELATION_ID
                               + " was specified both as explicit header and through the standard propagation of the mule "
                               + "correlation ID. The explicit header will prevail.");
            }
          } else {
            builder.addHeader(X_CORRELATION_ID, correlationId);
          }
        });

    if (config.isEnableCookies()) {
      try {
        Map<String, List<String>> headers =
            config.getCookieManager().get(builder.getUri(), emptyMap());
        List<String> cookies = headers.get(COOKIE);
        if (cookies != null) {
          for (String cookie : cookies) {
            builder.addHeader(COOKIE, cookie);
          }
        }
      } catch (IOException e) {
        LOGGER.warn("Error reading cookies for URI " + uri, e);
      }

    }

    try {
      builder.entity(createRequestEntity(streamingMode, sendBodyMode, transformationService, builder, method,
                                         requestBuilder.getBody()));
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

  private HttpEntity createRequestEntity(HttpStreamingType streamingMode, HttpSendBodyMode sendBodyMode,
                                         TransformationService transformationService, HttpRequestBuilder requestBuilder,
                                         String resolvedMethod, TypedValue<?> body) {
    HttpEntity entity;

    Object payload = body.getValue();
    Optional<Long> length = body.getLength();
    Optional<String> transferEncoding = requestBuilder.getHeaderValue(TRANSFER_ENCODING_HEADER);
    Optional<String> contentLength = requestBuilder.getHeaderValue(CONTENT_LENGTH_HEADER);

    if (isEmptyBody(payload, resolvedMethod, sendBodyMode)) {
      entity = new EmptyHttpEntity();
    } else if (payload instanceof InputStream || payload instanceof CursorStreamProvider) {
      payload = payload instanceof CursorStreamProvider ? ((CursorStreamProvider) payload).openCursor() : payload;
      if (streamingMode == ALWAYS) {
        entity = guaranteeStreaming(requestBuilder, transferEncoding, contentLength, (InputStream) payload);
      } else if (streamingMode == AUTO) {
        if (contentLength.isPresent()) {
          sanitizeForContentLength(requestBuilder, transferEncoding, BOTH_TRANSFER_HEADERS_SET_MESSAGE);
          entity = new ByteArrayHttpEntity(getPayloadAsBytes(payload, transformationService));
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
          entity = new ByteArrayHttpEntity(getPayloadAsBytes(payload, transformationService));
        }
      }
    } else {
      byte[] payloadAsBytes = getPayloadAsBytes(payload, transformationService);
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

  private boolean isEmptyBody(Object body, String method, HttpSendBodyMode sendBodyMode) {
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

  private byte[] getPayloadAsBytes(Object payload, TransformationService transformationService) {
    return (byte[]) transformationService.transform(of(payload), BYTE_ARRAY).getPayload().getValue();
  }

  private void sanitizeForStreaming(HttpRequestBuilder requestBuilder, Optional<String> transferEncodingHeader,
                                    Optional<String> contentLengthHeader) {
    if (contentLengthHeader != null) {
      requestBuilder.removeHeader(CONTENT_LENGTH);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content-Length header will not be sent, as the configured requestStreamingMode is ALWAYS.");
      }
    }

    if (transferEncodingHeader.isPresent() && !transferEncodingHeader.get().equalsIgnoreCase(CHUNKED)) {
      requestBuilder.removeHeader(TRANSFER_ENCODING);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Transfer-Encoding header will be sent with value 'chunked' instead of {}, as the configured "
            + "requestStreamingMode is ALWAYS", transferEncodingHeader);
      }

    }
  }

  private void sanitizeForContentLength(HttpRequestBuilder requestBuilder, Optional<String> transferEncoding, String reason) {
    if (transferEncoding.isPresent()) {
      requestBuilder.removeHeader(TRANSFER_ENCODING);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(reason);
      }
    }
  }
}
