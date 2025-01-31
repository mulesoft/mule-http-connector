/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.server;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import org.slf4j.Logger;

@Alias("server")
public class HTTPMockServerConnectionProvider implements CachedConnectionProvider<HTTPMockServer> {

  private static final Logger LOGGER = getLogger(HTTPMockServerConnectionProvider.class);

  @Parameter
  private Integer port;

  @Override
  public HTTPMockServer connect() {
    try {
      return new HTTPMockServer(port);
    } catch (Exception e) {
      LOGGER.error("Error creating the server", e);
      return null;
    }
  }

  @Override
  public void disconnect(HTTPMockServer server) {
    try {
      server.invalidate();
    } catch (Exception e) {
      LOGGER.error("Error while disconnecting", e);
    }
  }

  @Override
  public ConnectionValidationResult validate(HTTPMockServer connection) {
    return ConnectionValidationResult.success();
  }
}
