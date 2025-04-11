/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.internal.service.message.HttpEntityProxy;
import org.mule.extension.http.internal.service.server.RequestContext;
import org.mule.extension.http.internal.service.server.HttpRequestProxy;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;

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

  public static Result<InputStream, HttpRequestAttributes> transform(final RequestContext requestContext,
                                                                     final Charset encoding,
                                                                     ListenerPath listenerPath) {
    MediaType mediaType = getMediaType(requestContext.getHeaderValue(CONTENT_TYPE), encoding);

    final HttpEntityProxy entity = requestContext.getEntity();
    InputStream payload = entity.getContent();

    HttpRequestAttributes attributes =
        new HttpRequestAttributesResolver().setRequestContext(requestContext).setListenerPath(listenerPath).resolve();

    Result.Builder<InputStream, HttpRequestAttributes> resultBuilder = Result.builder();
    if (entity.getLength().isPresent()) {
      resultBuilder.length(entity.getLength().getAsLong());
    }

    return resultBuilder.output(payload).mediaType(mediaType).attributes(attributes).build();
  }

  /**
   *
   * @param contentTypeValue
   * @param defaultCharset   the encoding to use if the given {@code contentTypeValue} doesn't have a {@code charset} parameter.
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
