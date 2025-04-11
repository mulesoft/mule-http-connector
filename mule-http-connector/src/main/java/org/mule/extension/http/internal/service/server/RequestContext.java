/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import org.mule.extension.http.internal.service.message.HttpEntityProxy;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.HttpProtocol;
import org.mule.sdk.api.http.domain.message.request.HttpRequestContext;

import java.net.InetSocketAddress;
import java.net.URI;
import java.security.cert.Certificate;

public interface RequestContext {

  static RequestContext forSdkApi(org.mule.sdk.api.http.domain.message.request.HttpRequestContext ctx) {
    return new ImplementationForSdkApi(ctx);
  }

  static RequestContext forMuleApi(org.mule.runtime.http.api.domain.request.HttpRequestContext ctx) {
    return new ImplementationForMuleApi(ctx);
  }

  String getHeaderValue(String headerName);

  HttpEntityProxy getEntity();

  URI getUri();

  String getMethod();

  HttpProtocol getProtocol();

  MultiMap<String, String> getHeaders();

  InetSocketAddress getClientAddress();

  Certificate getClientCertificate();

  String getScheme();

  InetSocketAddress getServerAddress();

  class ImplementationForSdkApi implements RequestContext {

    private final HttpRequestContext delegate;

    public ImplementationForSdkApi(HttpRequestContext delegate) {
      this.delegate = delegate;
    }

    @Override
    public String getHeaderValue(String headerName) {
      return delegate.getRequest().getHeaderValue(headerName);
    }

    @Override
    public HttpEntityProxy getEntity() {
      return HttpEntityProxy.forSdkApi(delegate.getRequest().getEntity());
    }

    @Override
    public URI getUri() {
      return delegate.getRequest().getUri();
    }

    @Override
    public String getMethod() {
      return delegate.getRequest().getMethod();
    }

    @Override
    public HttpProtocol getProtocol() {
      return HttpProtocol.valueOf(delegate.getRequest().getProtocol().name());
    }

    @Override
    public MultiMap<String, String> getHeaders() {
      return delegate.getRequest().getHeaders();
    }

    @Override
    public InetSocketAddress getClientAddress() {
      return delegate.getClientConnection().getRemoteHostAddress();
    }

    @Override
    public Certificate getClientCertificate() {
      return delegate.getClientConnection().getClientCertificate();
    }

    @Override
    public String getScheme() {
      return delegate.getScheme();
    }

    @Override
    public InetSocketAddress getServerAddress() {
      return delegate.getServerConnection().getLocalHostAddress();
    }
  }

  class ImplementationForMuleApi implements RequestContext {

    private final org.mule.runtime.http.api.domain.request.HttpRequestContext delegate;

    public ImplementationForMuleApi(org.mule.runtime.http.api.domain.request.HttpRequestContext delegate) {
      this.delegate = delegate;
    }


    @Override
    public String getHeaderValue(String headerName) {
      return delegate.getRequest().getHeaderValue(headerName);
    }

    @Override
    public HttpEntityProxy getEntity() {
      return HttpEntityProxy.forMuleApi(delegate.getRequest().getEntity());
    }

    @Override
    public URI getUri() {
      return delegate.getRequest().getUri();
    }

    @Override
    public String getMethod() {
      return delegate.getRequest().getMethod();
    }

    @Override
    public HttpProtocol getProtocol() {
      return delegate.getRequest().getProtocol();
    }

    @Override
    public MultiMap<String, String> getHeaders() {
      return delegate.getRequest().getHeaders();
    }

    @Override
    public InetSocketAddress getClientAddress() {
      return delegate.getClientConnection().getRemoteHostAddress();
    }

    @Override
    public Certificate getClientCertificate() {
      return delegate.getClientConnection().getClientCertificate();
    }

    @Override
    public String getScheme() {
      return delegate.getScheme();
    }

    @Override
    public InetSocketAddress getServerAddress() {
      return delegate.getServerConnection().getLocalHostAddress();
    }
  }
}
