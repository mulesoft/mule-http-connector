/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;

import org.mule.runtime.api.util.MultiMap;

import java.security.cert.Certificate;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Builder for {@link HttpRequestAttributes}.
 *
 * @since 1.1
 */
public class HttpRequestAttributesBuilder {

  private static final char SLASH = '/';
  private static final String WILDCARD = "*";

  private MultiMap<String, String> headers = emptyMultiMap();
  private MultiMap<String, String> queryParams = emptyMultiMap();
  private Map<String, String> uriParams = emptyMap();
  private String requestPath;
  private String listenerPath;
  private String relativePath;
  private String maskedRequestPath;
  private String version;
  private String scheme;
  private String method;
  private String requestUri;
  private String rawRequestUri;
  private String queryString = "";
  private String localAddress;
  private String remoteAddress;
  private Supplier<Certificate> clientCertificate = () -> null;

  private boolean resolveMaskedRequestPath = false;

  public HttpRequestAttributesBuilder() {}

  public HttpRequestAttributesBuilder(HttpRequestAttributes requestAttributes) {
    this.headers = requestAttributes.getHeaders();
    this.queryParams = requestAttributes.getQueryParams();
    this.uriParams = requestAttributes.getUriParams();
    this.requestPath = requestAttributes.getRequestPath();
    this.listenerPath = requestAttributes.getListenerPath();
    this.relativePath = requestAttributes.getRelativePath();
    this.maskedRequestPath = requestAttributes.getMaskedRequestPath();
    this.version = requestAttributes.getVersion();
    this.scheme = requestAttributes.getScheme();
    this.method = requestAttributes.getMethod();
    this.requestUri = requestAttributes.getRequestUri();
    this.queryString = requestAttributes.getQueryString();
    this.localAddress = requestAttributes.getLocalAddress();
    this.remoteAddress = requestAttributes.getRemoteAddress();
    this.clientCertificate = requestAttributes::getClientCertificate;
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
    resolveMaskedRequestPath = true;
    return this;
  }

  public HttpRequestAttributesBuilder listenerPath(String listenerPath) {
    this.listenerPath = listenerPath;
    resolveMaskedRequestPath = true;
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

  public HttpRequestAttributesBuilder rawRequestUri(String rawRequestUri) {
    this.rawRequestUri = rawRequestUri;
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
    this.clientCertificate = () -> clientCertificate;
    return this;
  }

  /**
   * Allows establishing a lazily calculated certificate, avoiding SSL work until the {@link Certificate} is actually needed.
   *
   * @param clientCertificate a {@link Supplier} of the client {@link Certificate}
   * @return this builder
   * @since 1.4.0
   */
  public HttpRequestAttributesBuilder clientCertificate(Supplier<Certificate> clientCertificate) {
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
    if (resolveMaskedRequestPath && listenerPath != null && requestPath != null) {
      maskedRequestPath = maskRequestPath();
    }
    resolveMaskedRequestPath = false;
    return new HttpRequestAttributes(headers, listenerPath, relativePath, maskedRequestPath, version, scheme, method, requestPath,
                                     requestUri, rawRequestUri, queryString, queryParams, uriParams, localAddress, remoteAddress,
                                     clientCertificate);
  }

  private String maskRequestPath() {
    //Avoid resolution if not a valid listenerPath mask
    if (!listenerPath.endsWith(WILDCARD)) {
      return null;
    }

    int listenerPathCurrentSlashIndex = 0;
    int requestPathCurrentSlashIndex = 0;

    try {
      while (listenerPathCurrentSlashIndex < listenerPath.length() - 1) {
        listenerPathCurrentSlashIndex = iterateUntilNextSlash(listenerPath, listenerPathCurrentSlashIndex);
        requestPathCurrentSlashIndex = iterateUntilNextSlash(requestPath, requestPathCurrentSlashIndex);
      }
    } catch (StringIndexOutOfBoundsException e) {
      //If here it means that the number of slashes in the requestPath is not the same as in the listenerPath.
      //That can only happen if the requestPath is equal to the listenerPath without considering the *.
      return valueOf(SLASH);
    }

    return requestPath.substring(requestPathCurrentSlashIndex - 1);
  }

  private int iterateUntilNextSlash(String path, int position) {
    while (path.charAt(position) != SLASH) {
      position++;
    }
    position++;
    return position;
  }

}
