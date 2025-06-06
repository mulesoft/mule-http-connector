/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.sdk.api.http.HttpHeaders.Names.CONTENT_TYPE;

import static java.lang.Boolean.parseBoolean;
import static java.nio.charset.Charset.defaultCharset;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.sdk.api.http.domain.entity.HttpEntity;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.http.domain.message.request.HttpRequestContext;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that transforms an HTTP request to a proper {@link Result}.
 *
 * @since 1.0
 */
public class HttpRequestToResult {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestToResult.class);

  private HttpRequestToResult() {
    // private constructor to avoid wrong instantiations.
  }

  public static Result<InputStream, HttpRequestAttributes> transform(final HttpRequestContext requestContext,
                                                                     final Charset encoding,
                                                                     ListenerPath listenerPath) {
    final HttpRequest request = requestContext.getRequest();

    MediaType mediaType = getMediaType(request.getHeaderValue(CONTENT_TYPE), encoding);

    final HttpEntity entity = request.getEntity();
    InputStream payload = entity.getContent();

    HttpRequestAttributes attributes =
        new HttpRequestAttributesResolver().setRequestContext(requestContext).setListenerPath(listenerPath).resolve();

    Result.Builder<InputStream, HttpRequestAttributes> resultBuilder = Result.builder();
    entity.getBytesLength().ifPresent(resultBuilder::length);

    return resultBuilder.output(payload).mediaType(mediaType).attributes(attributes).build();
  }

  /**
   *
   * @param contentTypeValue the content type.
   * @param defaultCharset   the encoding to use if the given {@code contentTypeValue} doesn't have a {@code charset} parameter.
   * @return the media type.
   */
  public static MediaType getMediaType(final String contentTypeValue, Charset defaultCharset) {
    MediaType mediaType = MediaType.ANY;

    if (contentTypeValue != null) {
      try {
        mediaType = MediaType.parse(contentTypeValue);
      } catch (IllegalArgumentException e) {
        // need to support invalid Content-Types
        if (parseBoolean(System.getProperty(SYSTEM_PROPERTY_PREFIX + "strictContentType"))) {
          throw e;
        } else {
          LOGGER.warn("{} when parsing Content-Type '{}': {}", e.getClass().getName(), contentTypeValue, e.getMessage());
          LOGGER.warn("Using default encoding: {}", defaultCharset().name());
        }
      }
    }
    if (!mediaType.getCharset().isPresent()) {
      return mediaType.withCharset(defaultCharset);
    } else {
      return mediaType;
    }
  }

}
