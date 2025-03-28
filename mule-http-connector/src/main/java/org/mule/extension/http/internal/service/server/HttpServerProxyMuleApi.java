package org.mule.extension.http.internal.service.server;

import org.mule.runtime.http.api.server.HttpServer;

public class HttpServerProxyMuleApi implements HttpServerProxy {

    private final HttpServer delegate;

    public HttpServerProxyMuleApi(HttpServer delegate) {
        this.delegate = delegate;
    }
}
