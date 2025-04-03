/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.server;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.test.extensions.http.mock.internal.server.DelegateToFlowTransformer.RESPONSE_FUTURE_PARAMETER;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
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

import org.slf4j.Logger;

/**
 * A MuleSoft source that exposes a mock HTTP server endpoint for testing flows.
 *
 * <p>
 * This source is intended for use in MUnit tests, where an embedded HTTP server responds to incoming requests and delegates the
 * request payload to the configured Mule flow using {@link SourceCallback}.
 * </p>
 *
 * <p>
 * It registers a path-specific handler on startup and removes it on stop. The embedded server is provided by the
 * {@link HTTPMockServer} connection, allowing central server management across multiple endpoint sources.
 * </p>
 *
 * <p>
 * Responses from flows are asynchronously collected and sent back to the corresponding HTTP clients using a
 * {@link CompletableFuture} mechanism.
 * </p>
 *
 * Supported operations:
 * <ul>
 * <li>Registers an endpoint path on start</li>
 * <li>Removes the endpoint path on stop</li>
 * <li>Completes async response on success</li>
 * </ul>
 *
 * DSL Alias: {@code server-endpoint}
 */
@MediaType(value = ANY, strict = false)
@Alias("server-endpoint")
public class HTTPMockServerEndpointSource
    extends Source<InputStream, HTTPMockRequestAttributes> {

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
  public void onStart(SourceCallback<InputStream, HTTPMockRequestAttributes> sourceCallback) throws MuleException {
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
    LOGGER.warn("TERMINATE CALLED, BUT NOT IMPLEMENTED");
  }

  @OnSuccess
  public void completeResponse(@ParameterGroup(name = "response", showInDsl = true) HTTPMockServerResponse response,
                               SourceCallbackContext callbackContext) {
    LOGGER.info("Completing response from flow...");
    Optional<CompletableFuture<HTTPMockServerResponse>> responseFutureOptional =
        callbackContext.getVariable(RESPONSE_FUTURE_PARAMETER);

    responseFutureOptional.orElseThrow(
                                       () -> new IllegalStateException("Source callback context doesn't have the response future"))
        .complete(response);
  }
}
