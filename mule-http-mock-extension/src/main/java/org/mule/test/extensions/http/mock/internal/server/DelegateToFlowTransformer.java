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
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;

/**
 * WireMock transformer used to delegate the request received in the server-endpoint source to the rest of the flow.
 */
public class DelegateToFlowTransformer extends ResponseDefinitionTransformer {

  public static final String TRANSFORMER_NAME = "delegate-to-flow-transformer";
  public static final String SOURCE_CALLBACK_WIREMOCK_PARAMETER = "source-callback";
  public static final String RESPONSE_FUTURE_PARAMETER = "response-future";

  private static final Logger LOGGER = getLogger(DelegateToFlowTransformer.class);

  @Override
  public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files,
                                      Parameters parameters) {
    LOGGER.debug("transform() called");

    SourceCallback<InputStream, HTTPMockRequestAttributes> sourceCallback =
        (SourceCallback) parameters.get(SOURCE_CALLBACK_WIREMOCK_PARAMETER);

    if (sourceCallback == null) {
      LOGGER.error("SourceCallback is missing in the parameters");
      return ResponseDefinitionBuilder
          .like(responseDefinition)
          .but()
          .withStatus(500)
          .withBody("Source callback not found")
          .build();
    }

    SourceCallbackContext sourceContext = sourceCallback.createContext();

    CompletableFuture<ResponseDefinition> responseFuture = new CompletableFuture<>();
    sourceContext.addVariable(RESPONSE_FUTURE_PARAMETER, responseFuture);

    sourceCallback.handle(Result.<InputStream, HTTPMockRequestAttributes>builder()
        .output(getBodyAsInputStream(request))
        .attributes(getRequestAttributes(request))
        .build(), sourceContext);

    try {
      ResponseDefinition flowResponse = responseFuture.get();
      LOGGER.debug("Flow completed successfully with response: {}", flowResponse);
      return flowResponse;
    } catch (InterruptedException e) {
      LOGGER.error("Interrupted while waiting for the flow to complete", e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      LOGGER.error("Execution exception while waiting for the flow to complete", e);
    }

    return ResponseDefinitionBuilder
        .like(responseDefinition)
        .but()
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

  private static ByteArrayInputStream getBodyAsInputStream(Request request) {
    byte[] bodyBytes = request.getBody();
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Received body as String: {}", new String(bodyBytes));
    }
    return new ByteArrayInputStream(bodyBytes);
  }

  private static HTTPMockRequestAttributes getRequestAttributes(Request request) {
    HTTPMockRequestAttributes attributes = new HTTPMockRequestAttributes();
    // TODO: Extract and populate attributes from the request if necessary
    return attributes;
  }
}
