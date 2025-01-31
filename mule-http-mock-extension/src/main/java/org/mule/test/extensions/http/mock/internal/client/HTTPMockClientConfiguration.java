package org.mule.test.extensions.http.mock.internal.client;

import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

/**
 * Configuration of a mock HTTP Client.
 */
@ConnectionProviders(HTTPMockClientConnectionProvider.class)
@Configuration(name = "clientConfig")
@Operations(HTTPMockClientOperations.class)
public class HTTPMockClientConfiguration {
}
