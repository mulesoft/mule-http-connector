/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.lang.System.lineSeparator;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.api.util.SerializableLazyValue;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.security.cert.Certificate;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Representation of an HTTP request message attributes.
 *
 * @since 1.0
 */
public class HttpRequestAttributes extends BaseHttpRequestAttributes {

  private static final long serialVersionUID = 7227330842640270811L;

  public static HttpRequestAttributesBuilder builder() {
    return new HttpRequestAttributesBuilder();
  }

  /**
   * Full path where the request was received. Former 'http.listener.path'.
   */
  @Parameter
  private final String listenerPath;

  /**
   * Full path requested, encoded as received.
   *
   * @since 1.5.0
   */
  @Parameter
  protected String rawRequestPath;

  /**
   * Path where the request was received, without considering the base path. Former 'http.relative.path'.
   */
  @Parameter
  private final String relativePath;

  /**
   * Path computed from masking the {@code rawRequestPath} with the {@code listenerPath} and taking the difference. Note that this
   * is only calculated when the {@code listenerPath} is open (ends with a wildcard) and will be {@code null} otherwise.
   *
   * @since 1.4.0
   */
  @Parameter
  private final String maskedRequestPath;

  /**
   * HTTP version of the request. Former 'http.version'.
   */
  @Parameter
  private final String version;

  /**
   * HTTP scheme of the request. Former 'http.scheme'.
   */
  @Parameter
  private final String scheme;

  /**
   * HTTP method of the request. Former 'http.method'.
   */
  @Parameter
  private final String method;

  /**
   * Full URI of the request. Former 'http.request.uri'.
   */
  @Parameter
  private final String requestUri;

  /**
   * Full URI of the request, encoded as received.
   *
   * @since 1.5.0
   */
  @Parameter
  private final String rawRequestUri;

  /**
   * Query string of the request. Former 'http.query.string'.
   */
  @Parameter
  private final String queryString;

  /**
   * Local host address from the server.
   */
  @Parameter
  private final String localAddress;

  /**
   * Remote host address from the sender. Former 'http.remote.address'.
   */
  @Parameter
  private final String remoteAddress;

  /**
   * Client certificate (if 2 way TLS is enabled). Former 'http.client.cert'.
   */
  @Parameter
  @Optional
  private Certificate clientCertificate;

  /**
   * Actual {@link Certificate} to use, avoid any processing until it's actually needed.
   */
  private final SerializableLazyValue<Certificate> lazyClientCertificate;

  /**
   * This constructor is for internal use only.
   *
   * @since 1.6.0
   */
  public HttpRequestAttributes() {
    this(emptyMultiMap(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, () -> null);
  }

  /**
   * @deprecated use {@link HttpRequestAttributesBuilder} instead
   */
  @Deprecated
  public HttpRequestAttributes(MultiMap<String, String> headers, String listenerPath, String relativePath, String version,
                               String scheme, String method, String requestPath, String requestUri, String queryString,
                               MultiMap<String, String> queryParams, Map<String, String> uriParams, String remoteAddress,
                               Certificate clientCertificate) {
    this(headers, listenerPath, relativePath, null, version, scheme, method, requestPath, "", requestUri, "", queryString,
         queryParams,
         uriParams, "", remoteAddress, () -> clientCertificate);
  }

  HttpRequestAttributes(MultiMap<String, String> headers, String listenerPath, String relativePath, String maskedRequestPath,
                        String version, String scheme, String method, String requestPath, String rawRequestPath,
                        String requestUri, String rawRequestUri, String queryString, MultiMap<String, String> queryParams,
                        Map<String, String> uriParams, String localAddress, String remoteAddress,
                        Supplier<Certificate> certificateSupplier) {
    super(headers, queryParams, uriParams, requestPath);
    this.listenerPath = listenerPath;
    this.rawRequestPath = rawRequestPath;
    this.relativePath = relativePath;
    this.maskedRequestPath = maskedRequestPath;
    this.version = version;
    this.scheme = scheme;
    this.method = method;
    this.requestUri = requestUri;
    this.rawRequestUri = rawRequestUri;
    this.queryString = queryString;
    this.localAddress = localAddress;
    this.remoteAddress = remoteAddress;
    this.lazyClientCertificate = new SerializableLazyValue<>(certificateSupplier);
  }

  public String getListenerPath() {
    return listenerPath;
  }

  public String getRelativePath() {
    return relativePath;
  }

  public String getRawRequestPath() {
    return rawRequestPath;
  }

  public String getMaskedRequestPath() {
    return maskedRequestPath;
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

  public String getRawRequestUri() {
    return rawRequestUri;
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
    this.clientCertificate = lazyClientCertificate.get();
    return this.clientCertificate;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getName()).append(lineSeparator()).append("{").append(lineSeparator())
        .append(TAB).append("Request path=").append(requestPath).append(lineSeparator())
        .append(TAB).append("Raw request path=").append(rawRequestPath).append(lineSeparator())
        .append(TAB).append("Method=").append(method).append(lineSeparator())
        .append(TAB).append("Listener path=").append(this.listenerPath).append(lineSeparator())
        .append(TAB).append("Local Address=").append(localAddress).append(lineSeparator())
        .append(TAB).append("Query String=").append(obfuscateQueryIfNecessary()).append(lineSeparator())
        .append(TAB).append("Relative Path=").append(this.relativePath).append(lineSeparator())
        .append(TAB).append("Masked Request Path=").append(this.maskedRequestPath).append(lineSeparator())
        .append(TAB).append("Remote Address=").append(this.remoteAddress).append(lineSeparator())
        .append(TAB).append("Request Uri=").append(this.requestUri).append(lineSeparator())
        .append(TAB).append("Raw request Uri=").append(this.rawRequestUri).append(lineSeparator())
        .append(TAB).append("Scheme=").append(scheme).append(lineSeparator())
        .append(TAB).append("Version=").append(this.version).append(lineSeparator());

    buildMapToString(headers, "Headers", headers.entryList().stream(), builder);
    buildMapToString(queryParams, "Query Parameters", queryParams.entryList().stream(), builder);
    buildMapToString(uriParams, "URI Parameters", uriParams.entrySet().stream(), builder);

    builder.append("}");

    return builder.toString();
  }

  private String obfuscateQueryIfNecessary() {
    if (queryParams.keySet().stream().anyMatch(key -> key.equals("pass") || key.equals("password") || key.contains("secret"))) {
      return "****";
    }
    return this.queryString;
  }
}
