/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http;

import static org.mule.extension.http.api.request.HttpSendBodyMode.AUTO;

import static java.util.concurrent.Executors.newFixedThreadPool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.internal.request.HttpClientReflection;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import org.junit.Test;

public class HttpClientReflectionTestCase extends AbstractMuleTestCase {

    private static final int POOL_SIZE = 15;

    private static final ExecutorService executorService = newFixedThreadPool(POOL_SIZE);

    @Test
    @Issue("W-15043656")
    @Description("Regression test: the HttpClientReflection was using always the same options builder")
    public void differentConcurrentInvocationsUseDifferentBuilders() throws ExecutionException, InterruptedException {
        Set<Integer> seenResponseTimeouts = new ConcurrentSkipListSet<>();
        HttpClient client = new TestHttpClientSavingTheResponseTimeout(seenResponseTimeouts);

        int sendAsyncCallsCount = 1000;
        callSendAsyncMultipleTimesConcurrentlyWithDifferentResponseTimeouts(client, sendAsyncCallsCount);
        assertThat(seenResponseTimeouts.size(), is(sendAsyncCallsCount));
    }

    private static void callSendAsyncMultipleTimesConcurrentlyWithDifferentResponseTimeouts(HttpClient client, int sendAsyncCallsCount) throws InterruptedException, ExecutionException {
        HttpRequest request = mock(HttpRequest.class);
        HttpAuthentication authentication = mock(HttpAuthentication.class);
        List<Future<?>> futureList = new ArrayList<>(sendAsyncCallsCount);
        for (int i = 0; i < sendAsyncCallsCount; ++i) {
            futureList.add(executorService.submit(new SendAsyncRunnable(client, request, i, true, authentication, AUTO)));
        }
        for (Future<?> future : futureList) {
            future.get();
        }
    }

    private static final class SendAsyncRunnable implements Runnable {

        private final HttpClient client;
        private final HttpRequest request;
        private final int responseTimeout;
        private final boolean followRedirects;
        private final HttpAuthentication authentication;
        private final HttpSendBodyMode sendBodyMode;

        private SendAsyncRunnable(HttpClient client, HttpRequest request, int responseTimeout, boolean followRedirects,
                                  HttpAuthentication authentication, HttpSendBodyMode sendBodyMode) {
            this.client = client;
            this.request = request;
            this.responseTimeout = responseTimeout;
            this.followRedirects = followRedirects;
            this.authentication = authentication;
            this.sendBodyMode = sendBodyMode;
        }

        @Override
        public void run() {
            HttpClientReflection.sendAsync(client, request, responseTimeout, followRedirects, authentication, sendBodyMode);
        }
    }

    private static class TestHttpClientSavingTheResponseTimeout implements HttpClient {

        private final Set<Integer> seenResponseTimeouts;

        public TestHttpClientSavingTheResponseTimeout(Set<Integer> seenResponseTimeouts) {
            this.seenResponseTimeouts = seenResponseTimeouts;
        }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public HttpResponse send(HttpRequest request, HttpRequestOptions options) throws IOException, TimeoutException {
            seenResponseTimeouts.add(options.getResponseTimeout());
            return mock(HttpResponse.class);
        }

        @Override
        public CompletableFuture<HttpResponse> sendAsync(HttpRequest request, HttpRequestOptions options) {
            seenResponseTimeouts.add(options.getResponseTimeout());
            return CompletableFuture.completedFuture(mock(HttpResponse.class));
        }
    }
}
