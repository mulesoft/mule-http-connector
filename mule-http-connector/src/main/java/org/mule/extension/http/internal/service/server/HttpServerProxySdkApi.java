package org.mule.extension.http.internal.service.server;

import org.mule.sdk.api.http.server.HttpServer;

public class HttpServerProxySdkApi implements HttpServerProxy {

    private final HttpServer delegate;

    public HttpServerProxySdkApi(HttpServer delegate) {
        this.delegate = delegate;
    }
}
