package org.mule.extension.http.internal.service.server;

import org.mule.runtime.http.api.server.RequestHandlerManager;

import java.io.IOException;
import java.util.List;

public interface HttpServerProxy {

    void start() throws IOException;
    void stop();
    boolean isStopped();
    boolean isStopping();
    void dispose();

    String getIp();
    int getPort();

    RequestHandlerManager addRequestHandler(List<String> list, String path, RequestHandlerProxy requestHandler);
}
