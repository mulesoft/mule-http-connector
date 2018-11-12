/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.nio.charset.Charset.defaultCharset;
import static org.mule.runtime.api.metadata.MediaType.BINARY;
import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.runtime.core.api.util.ClassUtils.memoize;
import static org.mule.runtime.core.api.util.StringUtils.isEmpty;
import static org.mule.runtime.core.api.util.SystemUtils.getDefaultEncoding;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.SET_COOKIE;
import static org.mule.runtime.http.api.HttpHeaders.Names.SET_COOKIE2;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.internal.request.builder.HttpResponseAttributesBuilder;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Component that transforms an HTTP response to a proper {@link Result}.
 *
 * @since 1.0
 */
public class HttpResponseToResult {

  private static final Logger logger = LoggerFactory.getLogger(HttpResponseToResult.class);

  private static final String BINARY_CONTENT_TYPE = BINARY.toRfcString();
  private static boolean STRICT_CONTENT_TYPE = parseBoolean(getProperty(SYSTEM_PROPERTY_PREFIX + "strictContentType"));

  private final Function<String, MediaType> parseMediaType = memoize(ctv -> parseMediaType(ctv), new ConcurrentHashMap<>());

  public Result<InputStream, HttpResponseAttributes> convert(HttpRequesterCookieConfig config, MuleContext muleContext,
                                                             HttpResponse response, URI uri) {
    String responseContentType = response.getHeaderValue(CONTENT_TYPE);

    HttpEntity entity = response.getEntity();

    if (isEmpty(responseContentType) && !empty(entity)) {
      // RFC-2616 specifies application/octet-stream as default when none is received
      responseContentType = BINARY_CONTENT_TYPE;
    }

    MediaType responseMediaType = getMediaType(responseContentType, getDefaultEncoding(muleContext));

    if (config.isEnableCookies()) {
      processCookies(config, response, uri);
    }

    HttpResponseAttributes responseAttributes = createAttributes(response);

    final Result.Builder<InputStream, HttpResponseAttributes> builder = Result.builder();
    builder.mediaType(responseMediaType);
    if (entity.getLength().isPresent()) {
      builder.length(entity.getLength().get());
    }

    return builder.output(entity.getContent()).attributes(responseAttributes).build();
  }

  private boolean empty(HttpEntity entity) {
    return entity.getLength().filter(length -> length <= 0).isPresent();
  }

  private HttpResponseAttributes createAttributes(HttpResponse response) {
    return new HttpResponseAttributesBuilder().setResponse(response).build();
  }

  private void processCookies(HttpRequesterCookieConfig config, HttpResponse response, URI uri) {
    Collection<String> setCookieHeader = response.getHeaderValues(SET_COOKIE);
    Collection<String> setCookie2Header = response.getHeaderValues(SET_COOKIE2);

    Map<String, List<String>> cookieHeaders = new HashMap<>();

    if (setCookieHeader != null) {
      cookieHeaders.put(SET_COOKIE, new ArrayList<>(setCookieHeader));
    }

    if (setCookie2Header != null) {
      cookieHeaders.put(SET_COOKIE2, new ArrayList<>(setCookie2Header));
    }

    try {
      config.getCookieManager().put(uri, cookieHeaders);
    } catch (IOException e) {
      logger.warn("Error storing cookies for URI " + uri, e);
    }
  }

  /**
   *
   * @param contentTypeValue
   * @param defaultCharset the encoding to use if the given {@code contentTypeValue} doesn't have a {@code charset} parameter.
   * @return
   */
  private MediaType getMediaType(final String contentTypeValue, Charset defaultCharset) {
    MediaType mediaType;
    if (contentTypeValue != null) {
      mediaType = parseMediaType.apply(contentTypeValue);
    } else {
      mediaType = MediaType.ANY;
    }

    if (!mediaType.getCharset().isPresent()) {
      return mediaType.withCharset(defaultCharset);
    } else {
      return mediaType;
    }
  }

  private MediaType parseMediaType(final String contentTypeValue) {
    try {
      return MediaType.parse(contentTypeValue);
    } catch (IllegalArgumentException e) {
      // need to support invalid Content-Types
      if (STRICT_CONTENT_TYPE) {
        throw e;
      } else {
        logger.warn(format("%s when parsing Content-Type '%s': %s", e.getClass().getName(), contentTypeValue, e.getMessage()));
        logger.warn(format("Using default encoding: %s", defaultCharset().name()));
        return MediaType.ANY;
      }
    }
  }

  public static void refreshSystemProperties() {
    STRICT_CONTENT_TYPE = parseBoolean(getProperty(SYSTEM_PROPERTY_PREFIX + "strictContentType"));
  }

}
