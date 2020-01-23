/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.http.api.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import org.mule.runtime.http.api.domain.message.response.HttpResponseBuilder;

import java.util.Collection;
import java.util.Set;

public class HttpResponseHeaderBuilder {

  // This is a Map instead of a Set because of MULE-15249
  // While already fixed, it might be present in older versions of the runtime used with this connector
  private static final Set<String> uniqueHeadersNames;

  static {
    CaseInsensitiveMultiMap uniqueHeadersMap = new CaseInsensitiveMultiMap();
    uniqueHeadersMap.put(TRANSFER_ENCODING, "");
    uniqueHeadersMap.put(CONTENT_LENGTH, "");
    uniqueHeadersMap.put(CONTENT_TYPE, "");
    uniqueHeadersMap.put(ACCESS_CONTROL_ALLOW_ORIGIN, "");
    uniqueHeadersNames = uniqueHeadersMap.keySet();
  }

  private final HttpResponseBuilder responseBuilder;

  public HttpResponseHeaderBuilder(HttpResponseBuilder responseBuilder) {
    this.responseBuilder = responseBuilder;
  }

  public void addHeader(String headerName, Collection<String> headerValue) {
    if (headerValue.size() > 1 || responseBuilder.getHeaderValue(headerName).isPresent()) {
      failIfHeaderDoesNotSupportMultipleValues(headerName);
    }

    responseBuilder.addHeaders(headerName, headerValue);
  }

  public void addHeader(String headerName, String headerValue) {
    responseBuilder.getHeaderValue(headerName)
        .ifPresent(value -> failIfHeaderDoesNotSupportMultipleValues(headerName));

    responseBuilder.addHeader(headerName, headerValue);
  }

  public Collection<String> removeHeader(String headerName) {
    Collection<String> values = responseBuilder.getHeaderValues(headerName);
    responseBuilder.removeHeader(headerName);
    return values;
  }

  private void failIfHeaderDoesNotSupportMultipleValues(String headerName) {
    if (uniqueHeadersNames.contains(headerName)) {
      throw new MuleRuntimeException(createStaticMessage("Header " + headerName + " does not support multiple values"));
    }
  }

  public String getContentType() {
    return getSimpleValue(CONTENT_TYPE);
  }

  public String getTransferEncoding() {
    return getSimpleValue(TRANSFER_ENCODING);
  }

  public String getContentLength() {
    return getSimpleValue(CONTENT_LENGTH);
  }

  private String getSimpleValue(String header) {
    return responseBuilder.getHeaderValue(header).orElse(null);
  }

  public void addContentType(String multipartFormData) {
    addHeader(CONTENT_TYPE, multipartFormData);
  }

  public void setContentLength(String calculatedContentLength) {
    removeHeader(CONTENT_LENGTH);
    addHeader(CONTENT_LENGTH, calculatedContentLength);
  }

}
