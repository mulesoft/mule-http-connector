package org.mule.test.extensions.http.mock.internal;

import static org.mule.sdk.api.annotation.param.MediaType.ANY;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.exception.MuleException;
import org.mule.sdk.api.annotation.Alias;
import org.mule.sdk.api.annotation.execution.OnSuccess;
import org.mule.sdk.api.annotation.execution.OnTerminate;
import org.mule.sdk.api.annotation.param.Config;
import org.mule.sdk.api.annotation.param.Connection;
import org.mule.sdk.api.annotation.param.MediaType;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.connectivity.ConnectionProvider;
import org.mule.sdk.api.runtime.source.Source;
import org.mule.sdk.api.runtime.source.SourceCallback;
import org.mule.sdk.api.runtime.source.SourceCallbackContext;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.slf4j.Logger;

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
    private StubMapping stubMapping;

    @Override
    public void onStart(SourceCallback sourceCallback) throws MuleException {
        mockServer = serverProvider.connect();
        stubMapping = mockServer.mock().stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withTransformer("delegate-to-flow-transformer", "source-callback", sourceCallback)));
    }

    @Override
    public void onStop() {
        mockServer.mock().removeStubMapping(stubMapping);
        serverProvider.disconnect(mockServer);
    }

    @OnTerminate
    public void onTerminate() {
        // TODO: What if...
        LOGGER.error("TERMINATE CALLED, BUT NOT IMPLEMENTED");
    }

    @OnSuccess
    public void completeResponse(SourceCallbackContext callbackContext) {
        LOGGER.info("Generating response...");

        Optional<CompletableFuture<ResponseDefinition>> responseFutureOptional = callbackContext.getVariable("response-future");

        ResponseDefinition responseDefinition = new ResponseDefinitionBuilder()
                .withHeader("MyHeader", "Transformed")
                .withStatus(200)
                .withBody("Hello Eze")
                .build();

        responseFutureOptional.get().complete(responseDefinition);
    }
}
