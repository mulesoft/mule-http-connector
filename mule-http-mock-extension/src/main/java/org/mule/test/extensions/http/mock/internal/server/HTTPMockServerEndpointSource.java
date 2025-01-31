package org.mule.test.extensions.http.mock.internal.server;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.test.extensions.http.mock.internal.server.DelegateToFlowTransformer.RESPONSE_FUTURE_PARAMETER;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.execution.OnTerminate;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;

/**
 * Source that receives HTTP requests to a certain endpoint, and forwards them to the rest of the flow.
 */
@MediaType(value = ANY, strict = false)
@Alias("server-endpoint")
public class HTTPMockServerEndpointSource extends Source<InputStream, HTTPMockRequestAttributes> {

    private static final Logger LOGGER = getLogger(HTTPMockServerEndpointSource.class);

    @Config
    private HTTPMockServerConfiguration config;

    @Connection
    private ConnectionProvider<HTTPMockServer> serverProvider;

    @Parameter
    private String path;

    private HTTPMockServer mockServer;
    private HTTPMockServer.StubRemover removeStubCallback;

    @Override
    public void onStart(SourceCallback sourceCallback) throws MuleException {
        mockServer = serverProvider.connect();
        removeStubCallback = mockServer.addHandlerFor(path, sourceCallback);
    }

    @Override
    public void onStop() {
        removeStubCallback.removeStub();
        serverProvider.disconnect(mockServer);
    }

    @OnTerminate
    public void onTerminate() {
        // TODO: What if...
        LOGGER.warn("TERMINATE CALLED, BUT NOT IMPLEMENTED");
    }

    @OnSuccess
    public void completeResponse(@ParameterGroup(name = "response", showInDsl = true) HTTPMockServerResponse response, SourceCallbackContext callbackContext) {
        LOGGER.info("Generating response...");

        ResponseDefinitionBuilder builder = new ResponseDefinitionBuilder();
        builder.withStatus(response.getStatusCode());
        builder.withStatusMessage(response.getReasonPhrase());
        response.getHeaders().forEach(builder::withHeader);
        builder.withBody(IOUtils.toString(response.getBody().getValue()));
        ResponseDefinition responseDefinition = builder.build();

        Optional<CompletableFuture<ResponseDefinition>> responseFutureOptional = callbackContext.getVariable(RESPONSE_FUTURE_PARAMETER);
        responseFutureOptional.orElseThrow(() -> new IllegalStateException("Source callback context doesn't have the response future")).complete(responseDefinition);
    }
}
