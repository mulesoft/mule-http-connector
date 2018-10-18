/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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
    String path = uri.getRawPath();
    String uriString = path;
    String relativePath = this.listenerPath.getRelativePath(path);

    ClientConnection clientConnection = requestContext.getClientConnection();

    String queryString = uri.getRawQuery();
    if (queryString != null) {
      uriString += "?" + queryString;
    } else {
      queryString = "";
    }

    return new HttpRequestAttributesBuilder()
        .listenerPath(listenerPath)
        .relativePath(relativePath)
        .requestPath(path)
        .requestUri(uriString)
        .method(request.getMethod())
        .scheme(requestContext.getScheme())
        .version(request.getProtocol().asString())
        .headers(request.getHeaders())
        .uriParams(decodeUriParams(listenerPath, path))
        .queryString(queryString)
        .queryParams(decodeQueryString(queryString))
        .localAddress(requestContext.getServerConnection().getLocalHostAddress().toString())
        .remoteAddress(clientConnection.getRemoteHostAddress().toString())
        .clientCertificate(clientConnection::getClientCertificate)
        .build();
  }
}
