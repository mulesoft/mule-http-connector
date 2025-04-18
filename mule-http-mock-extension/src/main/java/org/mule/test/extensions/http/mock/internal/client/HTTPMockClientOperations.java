/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.client;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Operations to make HTTP Requests.
 */
public class HTTPMockClientOperations {

  @MediaType(value = ANY, strict = false)
  public void doGet(@Connection HTTPMockClient client, String url,
                    CompletionCallback<InputStream, HttpClientResponseAttributes> callback) {
    Request request = new Request.Builder().url(url).build();

    client.mock().newCall(request).enqueue(new Callback() {

      @Override
      public void onFailure(Call call, IOException e) {
        callback.error(e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        HttpClientResponseAttributes attrs = new HttpClientResponseAttributes();
        attrs.setStatusCode(response.code());
        attrs.setReasonPhrase(response.message());
        MultiMap<String, String> headers = new MultiMap<>();
        response.headers().forEach(pair -> headers.put(pair.getFirst(), pair.getSecond()));
        attrs.setHeaders(headers);

        InputStream body = Objects.requireNonNull(response.body()).byteStream();

        Result<InputStream, HttpClientResponseAttributes> result =
            Result.<InputStream, HttpClientResponseAttributes>builder().attributes(attrs).output(body).build();

        callback.success(result);
      }
    });
  }

  @MediaType(value = ANY, strict = false)
  public void doPost(@Connection HTTPMockClient client, String url, String payload,
                     MultiMap<String, String> requestHeaders,
                     CompletionCallback<InputStream, HttpClientResponseAttributes> callback) {

    RequestBody requestBody = RequestBody.create(payload, okhttp3.MediaType.parse("application/json"));

    Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

    if (requestHeaders != null) {
      requestHeaders.forEach(requestBuilder::addHeader);
    }
    Request request = requestBuilder.build();
    client.mock().newCall(request).enqueue(new Callback() {

      @Override
      public void onFailure(Call call, IOException e) {
        callback.error(e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        HttpClientResponseAttributes attrs = new HttpClientResponseAttributes();
        attrs.setStatusCode(response.code());
        attrs.setReasonPhrase(response.message());
        MultiMap<String, String> headers = new MultiMap<>();
        response.headers().forEach(pair -> headers.put(pair.getFirst(), pair.getSecond()));
        attrs.setHeaders(headers);

        InputStream body = Objects.requireNonNull(response.body()).byteStream();

        Result<InputStream, HttpClientResponseAttributes> result =
            Result.<InputStream, HttpClientResponseAttributes>builder().attributes(attrs).output(body).build();

        callback.success(result);
      }
    });
  }
}
