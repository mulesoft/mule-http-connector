package org.mule.test.extensions.http.mock.internal;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.sdk.api.runtime.operation.Result;
import org.mule.sdk.api.runtime.source.SourceCallback;
import org.mule.sdk.api.runtime.source.SourceCallbackContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.slf4j.Logger;

public class DelegateToFlowTransformer implements ResponseDefinitionTransformerV2 {

    private static final Logger LOGGER = getLogger(DelegateToFlowTransformer.class);

    @Override
    public ResponseDefinition transform(ServeEvent serveEvent) {
        LOGGER.debug("transform() called");
        Parameters parameters = serveEvent.getTransformerParameters();
        SourceCallback<InputStream, HTTPMockRequestAttributes> sourceCallback = (SourceCallback) parameters.get("source-callback");
        SourceCallbackContext sourceContext = sourceCallback.createContext();

        CompletableFuture<ResponseDefinition> responseFuture = new CompletableFuture<>();
        sourceContext.addVariable("response-future", responseFuture);

        sourceCallback.handle(Result.<InputStream, HTTPMockRequestAttributes>builder()
                .output(getBodyAsInputStream(serveEvent))
                .attributes(getRequestAttributes(serveEvent))
                .build(), sourceContext);

        try {
            return responseFuture.get();
        } catch (InterruptedException e) {
            LOGGER.error("Error occurred while waiting for flow to finish", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            LOGGER.error("Error occurred while waiting for flow to finish", e);
        }

        return new ResponseDefinitionBuilder()
                .withStatus(500)
                .withBody("Error while waiting for flow to finish")
                .build();
    }

    @Override
    public String getName() {
        return "delegate-to-flow-transformer";
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    // TODO: Implement!
    private static ByteArrayInputStream getBodyAsInputStream(ServeEvent serveEvent) {
        return new ByteArrayInputStream("Hello World".getBytes());
    }

    // TODO: Implement!
    private static HTTPMockRequestAttributes getRequestAttributes(ServeEvent serveEvent) {
        return new HTTPMockRequestAttributes();
    }
}
