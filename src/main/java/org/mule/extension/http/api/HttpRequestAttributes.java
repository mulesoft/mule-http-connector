/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.lang.System.lineSeparator;

import org.mule.runtime.api.util.MultiMap;

import java.security.cert.Certificate;
import java.util.Map;

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
   * Local host address from the server.
   */
  private final String localAddress;
  /**
   * Remote host address from the sender. Former 'http.remote.address'.
   */
  private final String remoteAddress;
  /**
   * Client certificate (if 2 way TLS is enabled). Former 'http.client.cert'.
   */
  private final Certificate clientCertificate;

  /**
   * @deprecated use {@link HttpRequestAttributesBuilder} instead
   */
  @Deprecated
  public HttpRequestAttributes(MultiMap<String, String> headers, String listenerPath, String relativePath, String version,
                               String scheme, String method, String requestPath, String requestUri, String queryString,
                               MultiMap<String, String> queryParams, Map<String, String> uriParams, String remoteAddress,
                               Certificate clientCertificate) {
    this(headers, listenerPath, relativePath, version, scheme, method, requestPath, requestUri, queryString, queryParams,
         uriParams, "", remoteAddress, clientCertificate);
  }

  HttpRequestAttributes(MultiMap<String, String> headers, String listenerPath, String relativePath, String version,
                        String scheme, String method, String requestPath, String requestUri, String queryString,
                        MultiMap<String, String> queryParams, Map<String, String> uriParams, String localAddress,
                        String remoteAddress, Certificate clientCertificate) {
    super(headers, queryParams, uriParams, requestPath);
    this.listenerPath = listenerPath;
    this.relativePath = relativePath;
    this.version = version;
    this.scheme = scheme;
    this.method = method;
    this.requestUri = requestUri;
    this.queryString = queryString;
    this.localAddress = localAddress;
    this.remoteAddress = remoteAddress;
    this.clientCertificate = clientCertificate;
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

  public String getLocalAddress() {
    return localAddress;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public Certificate getClientCertificate() {
    return clientCertificate;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getName()).append(lineSeparator()).append("{").append(lineSeparator())
        .append(tab).append("Request path=").append(requestPath).append(lineSeparator())
        .append(tab).append("Method=").append(method).append(lineSeparator())
        .append(tab).append("Listener path=").append(this.listenerPath).append(lineSeparator())
        .append(tab).append("Local Address=").append(localAddress).append(lineSeparator())
        .append(tab).append("Query String=").append(this.queryString).append(lineSeparator())
        .append(tab).append("Relative Path=").append(this.relativePath).append(lineSeparator())
        .append(tab).append("Remote Address=").append(this.remoteAddress).append(lineSeparator())
        .append(tab).append("Request Uri=").append(this.requestUri).append(lineSeparator())
        .append(tab).append("Scheme=").append(scheme).append(lineSeparator())
        .append(tab).append("Version=").append(this.version).append(lineSeparator());

    buildMapToString(headers, "Headers", headers == null ? null : headers.entryList().stream(), builder);
    buildMapToString(queryParams, "Query Parameters", queryParams == null ? null : queryParams.entryList().stream(), builder);
    buildMapToString(uriParams, "URI Parameters", uriParams == null ? null : uriParams.entrySet().stream(), builder);

    builder.append("}");

    return builder.toString();
  }
}
