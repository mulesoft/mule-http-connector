/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.decodeQueryString;
import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.decodeUriParams;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.request.ClientConnection;
import org.mule.runtime.http.api.domain.request.HttpRequestContext;

import java.net.URI;

/**
 * Creates {@link HttpRequestAttributes} based on an {@link HttpRequestContext}, it's parts and a {@link ListenerPath}.
 */
public class HttpRequestAttributesResolver {

  private static final String QUERY = "?";

  private HttpRequestContext requestContext;
  private ListenerPath listenerPath;

  public HttpRequestAttributesResolver setRequestContext(HttpRequestContext requestContext) {
    this.requestContext = requestContext;
    return this;
  }

  public HttpRequestAttributesResolver setListenerPath(ListenerPath listenerPath) {
    this.listenerPath = listenerPath;
    return this;
  }

  public HttpRequestAttributes resolve() {

    String listenerPath = this.listenerPath.getResolvedPath();
    HttpRequest request = requestContext.getRequest();

    URI uri = request.getUri();
    String path = uri.getPath();
    String rawPath = uri.getRawPath();
    String uriString = path;
    String rawUriString = rawPath;
    String relativePath = this.listenerPath.getRelativePath(path);

    ClientConnection clientConnection = requestContext.getClientConnection();

    String queryString = uri.getQuery();
    String rawQuery = uri.getRawQuery();
    if (queryString != null) {
      uriString += QUERY + queryString;
      rawUriString += QUERY + rawQuery;
    } else {
      queryString = "";
    }

    return new HttpRequestAttributesBuilder()
        .listenerPath(listenerPath)
        .relativePath(relativePath)
        .requestPath(path)
        .rawRequestPath(rawPath)
        .requestUri(uriString)
        .rawRequestUri(rawUriString)
        .method(request.getMethod())
        .scheme(requestContext.getScheme())
        .version(request.getProtocol().asString())
        .headers(request.getHeaders())
        .uriParams(decodeUriParams(listenerPath, rawPath))
        .queryString(queryString)
        .queryParams(decodeQueryString(rawQuery))
        .localAddress(requestContext.getServerConnection().getLocalHostAddress().toString())
        .remoteAddress(clientConnection.getRemoteHostAddress().toString())
        .clientCertificate(clientConnection::getClientCertificate)
        .build();
  }
}
