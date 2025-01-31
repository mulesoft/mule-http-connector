/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.client;

import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;

/**
 * Connection provider that creates instances of {@link HTTPMockClient} instances. It's a {@link CachedConnectionProvider}.
 */
public class HTTPMockClientConnectionProvider implements CachedConnectionProvider<HTTPMockClient> {

  @Override
  public HTTPMockClient connect() throws ConnectionException {
    return new HTTPMockClient();
  }

  @Override
  public void disconnect(HTTPMockClient connection) {}

  @Override
  public ConnectionValidationResult validate(HTTPMockClient connection) {
    return ConnectionValidationResult.success();
  }
}
