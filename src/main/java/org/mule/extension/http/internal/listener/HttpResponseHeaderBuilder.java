/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static java.util.Arrays.asList;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.http.api.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.server.HttpServerProperties.PRESERVE_HEADER_CASE;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HttpResponseHeaderBuilder {

  private static final Set<String> uniqueHeadersNames =
      new HashSet(asList(TRANSFER_ENCODING.toLowerCase(),
                         CONTENT_LENGTH.toLowerCase(),
                         CONTENT_TYPE.toLowerCase(),
                         ACCESS_CONTROL_ALLOW_ORIGIN.toLowerCase()));

  private MultiMap<String, String> headers = new CaseInsensitiveMultiMap(!PRESERVE_HEADER_CASE);

  public void addHeader(String headerName, Collection<String> headerValue) {
    if (headerValue.size() > 1 || headers.containsKey(headerName)) {
      failIfHeaderDoesNotSupportMultipleValues(headerName);
    }

    headers.put(headerName, headerValue);
  }

  public void addHeader(String headerName, String headerValue) {
    if (headers.containsKey(headerName)) {
      failIfHeaderDoesNotSupportMultipleValues(headerName);
    }

    headers.put(headerName, headerValue);
  }

  public Collection<String> removeHeader(String headerName) {
    List<String> values = headers.getAll(headerName);
    headers.remove(headerName);
    return values;
  }

  private void failIfHeaderDoesNotSupportMultipleValues(String headerName) {
    if (uniqueHeadersNames.contains(headerName.toLowerCase())) {
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
    if (!headers.containsKey(header)) {
      return null;
    }
    return headers.get(header);
  }

  public void addContentType(String multipartFormData) {
    addHeader(CONTENT_TYPE, multipartFormData);
  }

  public void setContentLength(String calculatedContentLength) {
    removeHeader(CONTENT_LENGTH);
    addHeader(CONTENT_LENGTH, calculatedContentLength);
  }

  public Collection<String> getHeaderNames() {
    return headers.keySet();
  }

  public Collection<String> getHeader(String headerName) {
    return headers.getAll(headerName);
  }
}
