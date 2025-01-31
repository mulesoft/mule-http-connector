package org.mule.test.extensions.http.mock.internal.client;

import okhttp3.OkHttpClient;

public class HTTPMockClient {

    private final OkHttpClient client;

    public HTTPMockClient() {
        client = new OkHttpClient();
    }

    public OkHttpClient mock() {
        return client;
    }
}
