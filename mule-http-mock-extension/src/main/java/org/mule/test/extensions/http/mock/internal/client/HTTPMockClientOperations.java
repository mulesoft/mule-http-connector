package org.mule.test.extensions.http.mock.internal.client;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class HTTPMockClientOperations {

    @MediaType(value = ANY, strict = false)
    public String doGet(@Connection HTTPMockClient client, String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.mock().newCall(request).execute()) {
            return response.body().string();
        }
    }
}
