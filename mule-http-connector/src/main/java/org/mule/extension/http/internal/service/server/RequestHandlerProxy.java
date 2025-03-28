package org.mule.extension.http.internal.service.server;

import org.mule.extension.http.internal.ser.HttpResponseReadyCallbackProxy;

public interface RequestHandlerProxy {
    void handleRequest(HttpRequestContextProxy requestContext, HttpResponseReadyCallbackProxy responseCallback);
}
