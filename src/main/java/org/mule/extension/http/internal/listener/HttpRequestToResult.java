/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.request.HttpRequestContext;

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

  private static final Logger logger = LoggerFactory.getLogger(HttpRequestToResult.class);

  public static Result<InputStream, HttpRequestAttributes> transform(final HttpRequestContext requestContext,
                                                                     final Charset encoding,
                                                                     ListenerPath listenerPath) {
    final HttpRequest request = requestContext.getRequest();

    MediaType mediaType = getMediaType(request.getHeaderValueIgnoreCase(CONTENT_TYPE), encoding);

    final HttpEntity entity = request.getEntity();
    InputStream payload = entity.getContent();

    HttpRequestAttributes attributes =
        new HttpRequestAttributesBuilder().setRequestContext(requestContext).setListenerPath(listenerPath).build();

    return Result.<InputStream, HttpRequestAttributes>builder().output(payload).mediaType(mediaType).attributes(attributes)
        .build();
  }

  /**
   * 
   * @param contentTypeValue
   * @param defaultCharset the encoding to use if the given {@code contentTypeValue} doesn't have a {@code charset} parameter.
   * @return
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
          logger.warn(format("%s when parsing Content-Type '%s': %s", e.getClass().getName(), contentTypeValue, e.getMessage()));
          logger.warn(format("Using default encoding: %s", defaultCharset().name()));
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
