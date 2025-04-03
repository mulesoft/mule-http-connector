/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.server;

import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to delegate the request received in the server-endpoint source to the rest of the flow.
 */
public class DelegateToFlowTransformer {

  public static final String RESPONSE_FUTURE_PARAMETER = "response-future";

  private static final Logger LOGGER = LoggerFactory.getLogger(DelegateToFlowTransformer.class);

  private DelegateToFlowTransformer() {}

  public static HTTPMockServerResponse delegate(SourceCallback<InputStream, HTTPMockRequestAttributes> callback,
                                                byte[] requestBody) {
    SourceCallbackContext context = callback.createContext();
    CompletableFuture<HTTPMockServerResponse> future = new CompletableFuture<>();
    context.addVariable(RESPONSE_FUTURE_PARAMETER, future);

    callback.handle(Result.<InputStream, HTTPMockRequestAttributes>builder().output(new ByteArrayInputStream(requestBody))
        .attributes(new HTTPMockRequestAttributes()).build(), context);

    try {
      return future.get();
    } catch (InterruptedException e) {
      LOGGER.error("Interrupted while waiting for flow to complete", e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      LOGGER.error("Exception during flow execution", e);
    }

    HTTPMockServerResponse errorResponse = new HTTPMockServerResponse();
    errorResponse.setStatusCode(500);
    errorResponse.setReasonPhrase("Internal Server Error");
    return errorResponse;
  }
}
