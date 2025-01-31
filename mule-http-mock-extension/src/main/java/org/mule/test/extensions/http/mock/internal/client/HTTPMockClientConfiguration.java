/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
