/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.util.Arrays.copyOfRange;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import org.mule.runtime.api.util.MultiMap;

import java.security.cert.Certificate;
import java.util.Map;

/**
 * Builder for {@link HttpRequestAttributes}.
 *
 * @since 1.1
 */
public class HttpRequestAttributesBuilder {

  private MultiMap<String, String> headers = emptyMultiMap();
  private MultiMap<String, String> queryParams = emptyMultiMap();
  private Map<String, String> uriParams = emptyMap();
  private String requestPath;
  private String listenerPath;
  private String relativePath;
  private String proxyRequestPath;
  private String version;
  private String scheme;
  private String method;
  private String requestUri;
  private String queryString = "";
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
    this.proxyRequestPath = requestAttributes.getProxyRequestPath();
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
    requireNonNull(headers, "HTTP headers cannot be null.");
    this.headers = headers;
    return this;
  }

  public HttpRequestAttributesBuilder queryParams(MultiMap<String, String> queryParams) {
    requireNonNull(queryParams, "Query params cannot be null.");
    this.queryParams = queryParams;
    return this;
  }

  public HttpRequestAttributesBuilder uriParams(Map<String, String> uriParams) {
    requireNonNull(uriParams, "URI params cannot be null.");
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
    requireNonNull(queryString, "Query string cannot be null.");
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
    requireNonNull(listenerPath, "Listener path cannot be null.");
    requireNonNull(relativePath, "Relative path cannot be null.");
    requireNonNull(version, "HTTP version cannot be null.");
    requireNonNull(scheme, "Scheme cannot be null.");
    requireNonNull(method, "HTTP method cannot be null.");
    requireNonNull(requestPath, "Request path cannot be null.");
    requireNonNull(requestUri, "Request URI cannot be null.");
    requireNonNull(localAddress, "Local address cannot be null.");
    requireNonNull(remoteAddress, "Remote address cannot be null.");
    this.proxyRequestPath = resolveProxyRequestPath();
    return new HttpRequestAttributes(headers, listenerPath, relativePath, proxyRequestPath, version, scheme, method, requestPath,
                                     requestUri,
                                     queryString, queryParams, uriParams, localAddress, remoteAddress, clientCertificate);
  }

  private String resolveProxyRequestPath() {
    byte[] listenerPathBytes = listenerPath.getBytes();
    byte[] requestPathBytes = requestPath.getBytes();
    int listenerPathIndex = 0;
    int requestPathIndex = 0;
    try {
      while (listenerPathBytes[listenerPathIndex] != '*') {
        listenerPathIndex = iterateUntilSlash(listenerPathBytes, listenerPathIndex);
        requestPathIndex = iterateUntilSlash(requestPathBytes, requestPathIndex);
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      //We could reach an error here if paths are not formed consistently. E.g: The listenerPath does not ends with *.
      //In that case we return null and let other logic parse the path as if this never existed
      return null;
    }
    return new String(copyOfRange(requestPathBytes, requestPathIndex - 1, requestPathBytes.length));
  }

  private int iterateUntilSlash(byte[] bytes, int position) {
    while (bytes[position] != '/') {
      position++;
    }
    position++;
    return position;
  }

}
