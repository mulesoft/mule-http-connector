/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.api.error.HttpError.SECURITY;
import static org.mule.extension.http.api.error.HttpError.TRANSFORMATION;
import static org.mule.extension.http.api.streaming.HttpStreamingType.ALWAYS;
import static org.mule.extension.http.api.streaming.HttpStreamingType.AUTO;
import static org.mule.extension.http.api.streaming.HttpStreamingType.NEVER;
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.api.metadata.DataType.BYTE_ARRAY;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.api.util.Preconditions.checkNotNull;
import static org.mule.runtime.core.api.config.MuleProperties.MULE_CORRELATION_ID_PROPERTY;
import static org.mule.sdk.api.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.sdk.api.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.sdk.api.http.HttpHeaders.Names.COOKIE;
import static org.mule.sdk.api.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.sdk.api.http.HttpHeaders.Names.X_CORRELATION_ID;
import static org.mule.sdk.api.http.HttpHeaders.Values.CHUNKED;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.OptionalLong.empty;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.streaming.HttpStreamingType;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.sdk.api.http.domain.entity.HttpEntity;
import org.mule.sdk.api.http.domain.entity.HttpEntityFactory;
import org.mule.sdk.api.http.domain.entity.multipart.Part;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.http.domain.message.request.HttpRequestBuilder;
import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
  private static final String X_CORRELATION_ID_HEADER = X_CORRELATION_ID.toLowerCase();
  private static final String MULE_CORRELATION_ID_PROPERTY_HEADER = MULE_CORRELATION_ID_PROPERTY.toLowerCase();
  private static final String COOKIE_HEADER = COOKIE.toLowerCase();

  private static final Set<String> DEFAULT_EMPTY_BODY_METHODS = unmodifiableSet(new HashSet<>(asList("GET", "HEAD", "OPTIONS")));

  private static final String BOTH_TRANSFER_HEADERS_SET_MESSAGE =
      "Cannot send both Transfer-Encoding and Content-Length headers. Transfer-Encoding will not be sent.";
  private static final String TRANSFER_ENCODING_NOT_ALLOWED_WHEN_NEVER_MESSAGE =
      "Transfer-Encoding header will not be sent, as the configured requestStreamingMode is NEVER.";
  private static final String INVALID_TRANSFER_ENCODING_HEADER_MESSAGE =
      "Transfer-Encoding header value was invalid and will not be sent.";

  private static final String COOKIES_SEPARATOR = "; ";
  private final HttpEntityFactory entityFactory;

  private boolean logFirstIgnoredBody = true;

  public HttpRequestFactory(HttpEntityFactory entityFactory) {
    this.entityFactory = entityFactory;
  }

  /**
   * Creates an {@link HttpRequest}.
   *
   * @param httpRequestCreator The generic {@link RequestCreator} from the request component that should be used to create the
   *                           {@link HttpRequest}.
   * @param authentication     The {@link HttpRequestAuthentication} that should be used to create the {@link HttpRequest}.
   * @return an {@link HttpRequest} configured based on the parameters.
   * @throws MuleException if the request creation fails.
   */
  public HttpRequest create(HttpRequesterConfig config, String uri, String method, HttpStreamingType streamingMode,
                            HttpSendBodyMode sendBodyMode, TransformationService transformationService,
                            HttpRequestAuthentication authentication, Map<String, List<String>> injectedHeaders,
                            RequestCreator httpRequestCreator,
                            DistributedTraceContextManager distributedTraceContextManager) {
    HttpRequestBuilder builder = httpRequestCreator.createRequestBuilder(config)
        .uri(uri)
        .method(method);

    config.getDefaultHeaders()
        .forEach(header -> builder.addHeader(header.getKey(), header.getValue()));

    injectedHeaders.forEach((headerName, headerValues) -> {
      for (String headerValue : headerValues) {
        builder.addHeader(headerName, headerValue);
      }
    });

    config.getDefaultQueryParams().forEach(param -> builder.addQueryParam(param.getKey(), param.getValue()));

    httpRequestCreator.getCorrelationData().ifPresent(correlationData -> addCorrelationIdResolution(correlationData, builder));

    if (config.isEnableCookies()) {
      addCookiesHeader(config, uri, builder);
    }

    distributedTraceContextManager.getRemoteTraceContextMap().forEach(builder::addHeader);

    try {
      builder.entity(createRequestEntity(streamingMode, sendBodyMode, transformationService, builder, method,
                                         httpRequestCreator.getBody()));
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

  private void addCorrelationIdResolution(CorrelationData correlationData, HttpRequestBuilder builder) {
    correlationData.getSendCorrelationId()
        .getOutboundCorrelationId(correlationData.getCorrelationInfo(), correlationData.getCorrelationId())
        .ifPresent(correlationId -> {
          String xCorrelationId;
          if (builder.getHeaderValue(X_CORRELATION_ID_HEADER).isPresent()) {
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug(
                           X_CORRELATION_ID
                               + " was specified both as explicit header and through the standard propagation of the mule "
                               + "correlation ID. The explicit header will prevail.");
            }
            xCorrelationId = builder.getHeaderValue(X_CORRELATION_ID_HEADER).get();
          } else {
            xCorrelationId = correlationId;
            builder.addHeader(X_CORRELATION_ID_HEADER, correlationId);
          }
          builder.getHeaderValue(MULE_CORRELATION_ID_PROPERTY_HEADER)
              .ifPresent(muleCorrelationId -> LOGGER
                  .warn("Explicitly configured 'MULE_CORRELATION_ID: {}' header could interfere with 'X-Correlation-ID: {}' header.",
                        muleCorrelationId, xCorrelationId));
        });
  }

  private void addCookiesHeader(HttpRequesterConfig config, String uri, HttpRequestBuilder builder) {
    try {
      List<String> cookies = config.getCookieManager().get(builder.getUri(), emptyMap()).get(COOKIE);
      if (cookies != null && cookies.size() > 0) {
        // The RFC-6265, section 5.4 says:
        // If the user agent does attach a Cookie header field to an HTTP
        // request, the user agent MUST send the cookie-string (defined below)
        // as the value of the header field.
        //
        // So we should concatenate the cookies

        int totalHeaderLength = COOKIES_SEPARATOR.length() * (cookies.size() - 1);
        for (String cookie : cookies) {
          totalHeaderLength += cookie.length();
        }

        StringBuilder sb = new StringBuilder(totalHeaderLength).append(cookies.get(0));
        for (int index = 1; index < cookies.size(); ++index) {
          sb.append(COOKIES_SEPARATOR).append(cookies.get(index));
        }
        builder.addHeader(COOKIE_HEADER, sb.toString());
      }
    } catch (IOException e) {
      LOGGER.warn("Error reading cookies for URI " + uri, e);
    }
  }

  private HttpEntity createRequestEntity(HttpStreamingType streamingMode, HttpSendBodyMode sendBodyMode,
                                         TransformationService transformationService, HttpRequestBuilder requestBuilder,
                                         String resolvedMethod, TypedValue<?> body) {
    HttpEntity entity;

    Object payload = body.getValue();
    OptionalLong length = body.getByteLength();
    Optional<String> transferEncoding = requestBuilder.getHeaderValue(TRANSFER_ENCODING_HEADER);
    Optional<String> contentLength = requestBuilder.getHeaderValue(CONTENT_LENGTH_HEADER);

    boolean emptyBody = isEmptyBody(payload, resolvedMethod, sendBodyMode);

    MediaType mediaType = body.getDataType().getMediaType();
    if (!requestBuilder.getHeaderValue(CONTENT_TYPE_HEADER).isPresent() && !emptyBody && !ANY.matches(mediaType)) {
      requestBuilder.addHeader(CONTENT_TYPE_HEADER, mediaType.toRfcString());
    }

    // TODO This screams for a refactor into an Abstract Factory...
    if (emptyBody) {
      entity = entityFactory.emptyEntity();
    } else if (payload instanceof CursorStreamProvider) {
      if (streamingMode == ALWAYS) {
        entity = guaranteeStreaming(requestBuilder, transferEncoding, contentLength, (CursorStreamProvider) payload);
      } else if (streamingMode == AUTO) {
        if (contentLength.isPresent()) {
          sanitizeForContentLength(requestBuilder, transferEncoding, BOTH_TRANSFER_HEADERS_SET_MESSAGE);
          entity = entityFactory
              .fromByteArray(getPayloadAsBytes(((CursorStreamProvider) payload).openCursor(), transformationService));
        } else if ((transferEncoding.isPresent() && CHUNKED.equalsIgnoreCase(transferEncoding.get())) || !length.isPresent()) {
          entity = new RepeatableInputStreamHttpEntity((CursorStreamProvider) payload);
        } else {
          sanitizeForContentLength(requestBuilder, transferEncoding, INVALID_TRANSFER_ENCODING_HEADER_MESSAGE);
          entity = avoidConsumingPayload(requestBuilder, (CursorStreamProvider) payload, length);
        }
      } else {
        sanitizeForContentLength(requestBuilder, transferEncoding, TRANSFER_ENCODING_NOT_ALLOWED_WHEN_NEVER_MESSAGE);
        if (length.isPresent()) {
          entity = avoidConsumingPayload(requestBuilder, (CursorStreamProvider) payload, length);
        } else {
          entity = entityFactory
              .fromByteArray(getPayloadAsBytes(((CursorStreamProvider) payload).openCursor(), transformationService));
        }
      }
    } else if (payload instanceof InputStream) {
      if (streamingMode == ALWAYS) {
        entity = guaranteeStreaming(requestBuilder, transferEncoding, contentLength, (InputStream) payload);
      } else if (streamingMode == AUTO) {
        if (contentLength.isPresent()) {
          sanitizeForContentLength(requestBuilder, transferEncoding, BOTH_TRANSFER_HEADERS_SET_MESSAGE);
          entity = entityFactory.fromByteArray(getPayloadAsBytes(payload, transformationService));
        } else if ((transferEncoding.isPresent() && CHUNKED.equalsIgnoreCase(transferEncoding.get())) || !length.isPresent()) {
          entity = entityFactory.fromInputStream((InputStream) payload);
        } else {
          sanitizeForContentLength(requestBuilder, transferEncoding, INVALID_TRANSFER_ENCODING_HEADER_MESSAGE);
          entity = avoidConsumingPayload(requestBuilder, (InputStream) payload, length);
        }
      } else {
        sanitizeForContentLength(requestBuilder, transferEncoding, TRANSFER_ENCODING_NOT_ALLOWED_WHEN_NEVER_MESSAGE);
        if (length.isPresent()) {
          entity = avoidConsumingPayload(requestBuilder, (InputStream) payload, length);
        } else {
          entity = entityFactory.fromByteArray(getPayloadAsBytes(payload, transformationService));
        }
      }
    } else {
      byte[] payloadAsBytes = getPayloadAsBytes(payload, transformationService);
      if (streamingMode == ALWAYS) {
        entity = guaranteeStreaming(requestBuilder, transferEncoding, contentLength, new ByteArrayInputStream(payloadAsBytes));
      } else if (streamingMode == NEVER) {
        sanitizeForContentLength(requestBuilder, transferEncoding, TRANSFER_ENCODING_NOT_ALLOWED_WHEN_NEVER_MESSAGE);
        entity = entityFactory.fromByteArray(payloadAsBytes);
      } else {
        // AUTO is defined so we'll let the headers define the transfer type
        if (contentLength.isPresent() && transferEncoding.isPresent()) {
          sanitizeForContentLength(requestBuilder, transferEncoding, BOTH_TRANSFER_HEADERS_SET_MESSAGE);
        }
        entity = entityFactory.fromInputStream(new ByteArrayInputStream(payloadAsBytes), (long) payloadAsBytes.length);
      }
    }

    return entity;
  }

  private boolean isEmptyBody(Object body, String method, HttpSendBodyMode sendBodyMode) {
    boolean emptyBody;

    boolean hasBody = body != null && !body.equals("");
    if (!hasBody) {
      emptyBody = true;
    } else {
      emptyBody = DEFAULT_EMPTY_BODY_METHODS.contains(method);

      if (sendBodyMode != HttpSendBodyMode.AUTO) {
        emptyBody = (sendBodyMode == HttpSendBodyMode.NEVER);
      }
    }

    if (emptyBody && hasBody && logFirstIgnoredBody) {
      logFirstIgnoredBody = false;
      LOGGER.warn(
                  "Body is ignored since the HTTP Method is between the empty body methods ({}) and the Send Body Mode is not set to 'ALWAYS'. future warnings like this will be suppressed in order to avoid performance degradations",
                  DEFAULT_EMPTY_BODY_METHODS);
    }

    return emptyBody;
  }

  /**
   * Generates an {@link HttpEntity} with no length and sanitizes the headers for chunking
   */
  private HttpEntity guaranteeStreaming(HttpRequestBuilder requestBuilder, Optional<String> transferEncoding,
                                        Optional<String> contentLength, InputStream stream) {
    sanitizeForStreaming(requestBuilder, transferEncoding, contentLength);
    return entityFactory.fromInputStream(stream);
  }

  /**
   * Generates an {@link HttpEntity} with no length and sanitizes the headers for chunking
   */
  private HttpEntity guaranteeStreaming(HttpRequestBuilder requestBuilder, Optional<String> transferEncoding,
                                        Optional<String> contentLength, CursorStreamProvider streamProvider) {
    sanitizeForStreaming(requestBuilder, transferEncoding, contentLength);
    return new RepeatableInputStreamHttpEntity(streamProvider);
  }

  /**
   * Generates an {@link HttpEntity} with a length and sets the Content-Length header with it as well
   */
  private HttpEntity avoidConsumingPayload(HttpRequestBuilder requestBuilder, InputStream payload, OptionalLong length) {
    requestBuilder.addHeader(CONTENT_LENGTH, valueOf(length.getAsLong()));
    return entityFactory.fromInputStream(payload, length.getAsLong());
  }

  /**
   * Generates an {@link HttpEntity} with a length and sets the Content-Length header with it as well
   */
  private HttpEntity avoidConsumingPayload(HttpRequestBuilder requestBuilder, CursorStreamProvider streamProvider,
                                           OptionalLong length) {
    requestBuilder.addHeader(CONTENT_LENGTH, valueOf(length.getAsLong()));
    return new RepeatableInputStreamHttpEntity(streamProvider, length.getAsLong());
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

  private static class RepeatableInputStreamHttpEntity implements HttpEntity {

    private Long contentLength;
    private final CursorStreamProvider streamProvider;

    public RepeatableInputStreamHttpEntity(CursorStreamProvider streamProvider) {
      checkNotNull(streamProvider, "HTTP entity stream provider cannot be null.");
      this.streamProvider = streamProvider;
    }

    public RepeatableInputStreamHttpEntity(CursorStreamProvider streamProvider, Long contentLength) {
      this(streamProvider);
      this.contentLength = contentLength;
    }

    @Override
    public boolean isStreaming() {
      return true;
    }

    @Override
    public boolean isComposed() {
      return false;
    }

    @Override
    public InputStream getContent() {
      return streamProvider.openCursor();
    }

    @Override
    public byte[] getBytes() {
      return IOUtils.toByteArray(getContent());
    }

    @Override
    public Collection<Part> getParts() {
      return emptyList();
    }

    @Override
    public OptionalLong getBytesLength() {
      return contentLength == null ? empty() : OptionalLong.of(contentLength);
    }
  }
}
