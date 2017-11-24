/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import org.mule.runtime.api.util.MultiMap;

import java.security.cert.Certificate;
import java.util.Map;

/**
 * Builder for {@link HttpRequestAttributes}.
 *
 * @since 1.1
 */
public class HttpRequestAttributesBuilder {

  private MultiMap<String, String> headers;
  private MultiMap<String, String> queryParams;
  private Map<String, String> uriParams;
  private String requestPath;
  private String listenerPath;
  private String relativePath;
  private String version;
  private String scheme;
  private String method;
  private String requestUri;
  private String queryString;
  private String localAddress;
  private String remoteAddress;
  private Certificate clientCertificate;

  public HttpRequestAttributesBuilder() {}

  public HttpRequestAttributesBuilder(HttpRequestAttributes requestAttributes) {
    this.headers = requestAttributes.getHeaders();
    this.queryParams = requestAttributes.getQueryParams();
    this.uriParams = requestAttributes.getUriParams();
    this.requestPath = requestAttributes.getRequestPath();
    this.listenerPath = requestAttributes.getListenerPath();
    this.relativePath = requestAttributes.getRelativePath();
    this.version = requestAttributes.getVersion();
    this.scheme = requestAttributes.getScheme();
    this.method = requestAttributes.getMethod();
    this.requestUri = requestAttributes.getRequestUri();
    this.queryString = requestAttributes.getQueryString();
    this.localAddress = requestAttributes.getLocalAddress();
    this.remoteAddress = requestAttributes.getRemoteAddress();
    this.clientCertificate = requestAttributes.getClientCertificate();
  }

  public HttpRequestAttributesBuilder headers(MultiMap<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public HttpRequestAttributesBuilder queryParams(MultiMap<String, String> queryParams) {
    this.queryParams = queryParams;
    return this;
  }

  public HttpRequestAttributesBuilder uriParams(Map<String, String> uriParams) {
    this.uriParams = uriParams;
    return this;
  }

  public HttpRequestAttributesBuilder requestPath(String requestPath) {
    this.requestPath = requestPath;
    return this;
  }

  public HttpRequestAttributesBuilder listenerPath(String listenerPath) {
    this.listenerPath = listenerPath;
    return this;
  }

  public HttpRequestAttributesBuilder relativePath(String relativePath) {
    this.relativePath = relativePath;
    return this;
  }

  public HttpRequestAttributesBuilder version(String version) {
    this.version = version;
    return this;
  }

  public HttpRequestAttributesBuilder scheme(String scheme) {
    this.scheme = scheme;
    return this;
  }

  public HttpRequestAttributesBuilder method(String method) {
    this.method = method;
    return this;
  }

  public HttpRequestAttributesBuilder requestUri(String requestUri) {
    this.requestUri = requestUri;
    return this;
  }

  public HttpRequestAttributesBuilder queryString(String queryString) {
    this.queryString = queryString;
    return this;
  }

  public HttpRequestAttributesBuilder localAddress(String localAddress) {
    this.localAddress = localAddress;
    return this;
  }

  public HttpRequestAttributesBuilder remoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
    return this;
  }

  public HttpRequestAttributesBuilder clientCertificate(Certificate clientCertificate) {
    this.clientCertificate = clientCertificate;
    return this;
  }

  public HttpRequestAttributes build() {
    return new HttpRequestAttributes(headers, listenerPath, relativePath, version, scheme, method, requestPath, requestUri,
                                     queryString, queryParams, uriParams, localAddress, remoteAddress, clientCertificate);
  }
}
