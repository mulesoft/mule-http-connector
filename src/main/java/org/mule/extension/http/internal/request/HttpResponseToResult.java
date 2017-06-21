/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.metadata.MediaType.ANY;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA;
import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.runtime.core.api.util.StringUtils.isEmpty;
import static org.mule.runtime.core.api.util.SystemUtils.getDefaultEncoding;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.SET_COOKIE;
import static org.mule.runtime.http.api.HttpHeaders.Names.SET_COOKIE2;
import static org.mule.runtime.http.api.HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED;
import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.decodeUrlEncodedBody;
import static reactor.core.publisher.Mono.defer;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;
import static reactor.core.scheduler.Schedulers.fromExecutorService;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpMessageParsingException;
import org.mule.extension.http.internal.request.builder.HttpResponseAttributesBuilder;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.message.MultiPartPayload;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.api.message.DefaultMultiPartPayload;
import org.mule.runtime.core.api.message.PartAttributes;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Component that transforms an HTTP response to a proper {@link Result}.
 *
 * @since 1.0
 */
public class HttpResponseToResult {

  private static final Logger logger = LoggerFactory.getLogger(HttpResponseToResult.class);
  private static final String MULTI_PART_PREFIX = "multipart/";

  private final Boolean parseResponse;
  private final HttpRequesterCookieConfig config;
  private final MuleContext muleContext;

  public HttpResponseToResult(HttpRequesterCookieConfig config, Boolean parseResponse, MuleContext muleContext) {
    this.config = config;
    this.parseResponse = parseResponse;
    this.muleContext = muleContext;
  }

  public Publisher<Result<Object, HttpResponseAttributes>> convert(MediaType mediaType, HttpResponse response, String uri,
                                                                   Scheduler scheduler)
      throws HttpMessageParsingException {
    String responseContentType = response.getHeaderValueIgnoreCase(CONTENT_TYPE);
    if (isEmpty(responseContentType) && !ANY.matches(mediaType)) {
      responseContentType = mediaType.toRfcString();
    }
    MediaType responseMediaType = getMediaType(responseContentType, getDefaultEncoding(muleContext));

    HttpEntity entity = response.getEntity();
    Charset encoding = responseMediaType.getCharset().get();

    Mono<?> payload = just(entity.getContent());
    if (responseContentType != null && parseResponse) {
      if (entity.isComposed()) {
        responseMediaType = getJavaMediaType(encoding);
        // Given we need to read whole payload in this scenario, do this using IO scheduler for avoid deadlock
        payload = defer(() -> {
          try {
            return just(multiPartPayloadForAttachments(entity.getParts()));
          } catch (IOException e) {
            return error(new HttpMessageParsingException(createStaticMessage("Unable to process multipart response"), e));
          }
        }).subscribeOn(fromExecutorService(scheduler));
      } else if (responseContentType.startsWith(APPLICATION_X_WWW_FORM_URLENCODED.toRfcString())) {
        responseMediaType = getJavaMediaType(encoding);
        // Given we need to read whole payload in this scenario, do this using IO scheduler for avoid deadlock
        payload = defer(() -> just(decodeUrlEncodedBody(IOUtils.toString(entity.getContent()), encoding)))
            .subscribeOn(fromExecutorService(scheduler));
      }
    }

    if (config.isEnableCookies()) {
      processCookies(response, uri);
    }

    HttpResponseAttributes responseAttributes = createAttributes(response);

    mediaType = DataType.builder().mediaType(mediaType).charset(encoding).build().getMediaType();

    final Result.Builder builder = Result.builder();
    if (isEmpty(responseContentType)) {
      builder.mediaType(mediaType);
    } else {
      builder.mediaType(responseMediaType);
    }
    builder.attributes(responseAttributes);

    return payload.map(p -> builder.output(p).build());
  }

  private MediaType getJavaMediaType(Charset encoding) {
    return APPLICATION_JAVA.withCharset(encoding);
  }

  private static MultiPartPayload multiPartPayloadForAttachments(Collection<HttpPart> httpParts) throws IOException {
    List<org.mule.runtime.api.message.Message> parts = new ArrayList<>();

    int partNumber = 1;
    for (HttpPart httpPart : httpParts) {
      Map<String, LinkedList<String>> headers = new HashMap<>();
      for (String headerName : httpPart.getHeaderNames()) {
        if (!headers.containsKey(headerName)) {
          headers.put(headerName, new LinkedList<>());
        }
        headers.get(headerName).addAll(httpPart.getHeaders(headerName));
      }

      parts.add(Message.builder().payload(httpPart.getInputStream()).mediaType(MediaType.parse(httpPart.getContentType()))
          .attributes(new PartAttributes(httpPart.getName() != null ? httpPart.getName() : "part_" + partNumber,
                                         httpPart.getFileName(), httpPart.getSize(), headers))
          .build());

      partNumber++;
    }

    return new DefaultMultiPartPayload(parts);
  }

  private HttpResponseAttributes createAttributes(HttpResponse response) {
    return new HttpResponseAttributesBuilder().setResponse(response).build();
  }

  private void processCookies(HttpResponse response, String uri) {
    Collection<String> setCookieHeader = response.getHeaderValuesIgnoreCase(SET_COOKIE);
    Collection<String> setCookie2Header = response.getHeaderValuesIgnoreCase(SET_COOKIE2);

    Map<String, List<String>> cookieHeaders = new HashMap<>();

    if (setCookieHeader != null) {
      cookieHeaders.put(SET_COOKIE, new ArrayList<>(setCookieHeader));
    }

    if (setCookie2Header != null) {
      cookieHeaders.put(SET_COOKIE2, new ArrayList<>(setCookie2Header));
    }

    try {
      config.getCookieManager().put(URI.create(uri), cookieHeaders);
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
  private static MediaType getMediaType(final String contentTypeValue, Charset defaultCharset) {
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
