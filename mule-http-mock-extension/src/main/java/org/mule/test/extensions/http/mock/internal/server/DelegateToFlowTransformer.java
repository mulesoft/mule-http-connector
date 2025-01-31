/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.server;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;

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

/**
 * WireMock transformer used to delegate the request received in the server-endpoint source to the rest of the flow.
 */
public class DelegateToFlowTransformer implements ResponseDefinitionTransformerV2 {

    public static final String TRANSFORMER_NAME = "delegate-to-flow-transformer";
    public static final String SOURCE_CALLBACK_WIREMOCK_PARAMETER = "source-callback";
    public static final String RESPONSE_FUTURE_PARAMETER = "response-future";

    private static final Logger LOGGER = getLogger(DelegateToFlowTransformer.class);

    @Override
    public ResponseDefinition transform(ServeEvent serveEvent) {
        LOGGER.debug("transform() called");
        Parameters parameters = serveEvent.getTransformerParameters();
        SourceCallback<InputStream, HTTPMockRequestAttributes> sourceCallback = (SourceCallback) parameters.get(SOURCE_CALLBACK_WIREMOCK_PARAMETER);
        SourceCallbackContext sourceContext = sourceCallback.createContext();

        CompletableFuture<ResponseDefinition> responseFuture = new CompletableFuture<>();
        sourceContext.addVariable(RESPONSE_FUTURE_PARAMETER, responseFuture);

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
        return TRANSFORMER_NAME;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private static ByteArrayInputStream getBodyAsInputStream(ServeEvent serveEvent) {
        byte[] bodyBytes = serveEvent.getRequest().getBody();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Received body as String: {}", new String(bodyBytes));
        }
        return new ByteArrayInputStream(bodyBytes);
    }

    // TODO: Implement!
    private static HTTPMockRequestAttributes getRequestAttributes(ServeEvent serveEvent) {
        return new HTTPMockRequestAttributes();
    }
}
