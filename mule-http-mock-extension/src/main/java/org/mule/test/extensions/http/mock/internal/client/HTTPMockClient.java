/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.client;

import okhttp3.OkHttpClient;

/**
 * Mock HTTP Client based on OkHttp.
 */
public class HTTPMockClient {

    private final OkHttpClient client;

    public HTTPMockClient() {
        client = new OkHttpClient();
    }

    public OkHttpClient mock() {
        return client;
    }
}
