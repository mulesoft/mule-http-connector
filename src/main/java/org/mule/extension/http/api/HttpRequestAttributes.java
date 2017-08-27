/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import java.security.cert.Certificate;

import javax.net.ssl.SSLSession;

import org.mule.runtime.api.util.MultiMap;

/**
 * Representation of an HTTP request message attributes.
 *
 * @since 1.0
 */
public class HttpRequestAttributes extends BaseHttpRequestAttributes {

  private static final long serialVersionUID = 7227330842640270811L;

  /**
   * Full path where the request was received. Former 'http.listener.path'.
   */
  private final String listenerPath;
  /**
   * Path where the request was received, without considering the base path. Former 'http.relative.path'.
   */
  private final String relativePath;
  /**
   * HTTP version of the request. Former 'http.version'.
   */
  private final String version;
  /**
   * HTTP scheme of the request. Former 'http.scheme'.
   */
  private final String scheme;
  /**
   * HTTP method of the request. Former 'http.method'.
   */
  private final String method;
  /**
   * Full URI of the request. Former 'http.request.uri'.
   */
  private final String requestUri;
  /**
   * Query string of the request. Former 'http.query.string'.
   */
  private final String queryString;
  /**
   * Remote host address from the sender. Former 'http.remote.address'.
   */
  private final String remoteAddress;
  /**
   * Client certificate (if 2 way TLS is enabled). Former 'http.client.cert'.
   */
  private final Certificate clientCertificate;

  private final SSLSession sslSession;

  public HttpRequestAttributes(MultiMap<String, String> headers, String listenerPath, String relativePath, String version,
                               String scheme, String method, String requestPath, String requestUri, String queryString,
                               MultiMap<String, String> queryParams, MultiMap<String, String> uriParams,
                               String remoteAddress, Certificate clientCertificate, SSLSession sslSession) {
    super(headers, queryParams, uriParams, requestPath);
    this.listenerPath = listenerPath;
    this.relativePath = relativePath;
    this.version = version;
    this.scheme = scheme;
    this.method = method;
    this.requestUri = requestUri;
    this.queryString = queryString;
    this.remoteAddress = remoteAddress;
    this.clientCertificate = clientCertificate;
    this.sslSession = sslSession;
  }

  public String getListenerPath() {
    return listenerPath;
  }

  public String getRelativePath() {
    return relativePath;
  }

  public String getVersion() {
    return version;
  }

  public String getScheme() {
    return scheme;
  }

  public String getMethod() {
    return method;
  }

  public String getRequestUri() {
    return requestUri;
  }

  public String getQueryString() {
    return queryString;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public Certificate getClientCertificate() {
    return clientCertificate;
  }

  public SSLSession getSslSession() {
    return sslSession;
  }


}
