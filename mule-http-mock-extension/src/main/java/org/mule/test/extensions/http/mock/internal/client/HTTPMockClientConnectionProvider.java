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
    public void disconnect(HTTPMockClient connection) {
    }

    @Override
    public ConnectionValidationResult validate(HTTPMockClient connection) {
        return ConnectionValidationResult.success();
    }
}
