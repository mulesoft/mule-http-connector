package org.mule.test.extensions.http.mock.internal.client;

import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;

import org.mule.runtime.api.util.MultiMap;

public class HttpClientResponseAttributes {

    private int statusCode;
    private String reasonPhrase;
    private MultiMap<String, String> headers = emptyMultiMap();

    public HttpClientResponseAttributes() {
    }

    public MultiMap<String, String> getHeaders() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setHeaders(MultiMap<String, String> headers) {
        this.headers = headers;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    @Override
    public String toString() {
        return "HttpClientResponseAttributes{" +
                "statusCode=" + statusCode +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                ", headers=" + headers +
                '}';
    }
}
