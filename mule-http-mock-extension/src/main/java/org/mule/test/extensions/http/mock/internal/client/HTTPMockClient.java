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
