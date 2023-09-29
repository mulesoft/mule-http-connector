/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.internal.request;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.tck.junit4.matcher.IsEmptyOptional.empty;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.slf4j.LoggerFactory.getLogger;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mule.extension.http.internal.request.HttpRequesterConnectionManager;
import org.mule.extension.http.internal.request.ShareableHttpClient;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.client.HttpClientFactory;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Feature(HTTP_EXTENSION)
@Story("HTTP Request")
public class HttpRequesterConnectionManagerTestCase extends AbstractMuleTestCase {

  private final Logger LOGGER = getLogger(HttpRequesterConnectionManagerTestCase.class);

  private static final String CONFIG_NAME = "config";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private HttpService httpService = mock(HttpService.class);
  private HttpClient delegateHttpClient = spy(HttpClient.class);
  private HttpClient otherHttpClient = spy(HttpClient.class);
  private HttpRequesterConnectionManager connectionManager = new HttpRequesterConnectionManager(httpService);

  private AtomicBoolean clientFactoryIsSlow = new AtomicBoolean(false);

  @Before
  public void setUp() {
    HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
    when(httpService.getClientFactory()).thenReturn(httpClientFactory);
    when(httpClientFactory.create(any())).thenAnswer(
                                                     invocation -> {
                                                       if (clientFactoryIsSlow.get()) {
                                                         sleep(500L);
                                                       }
                                                       HttpClientConfiguration configuration =
                                                           (HttpClientConfiguration) invocation.getArguments()[0];
                                                       if (CONFIG_NAME.equals(configuration.getName())) {
                                                         return delegateHttpClient;
                                                       } else {
                                                         return otherHttpClient;
                                                       }
                                                     });
  }

  @Test
  public void lookup() {
    assertThat(connectionManager.lookup(CONFIG_NAME), is(empty()));
    ShareableHttpClient client = connectionManager.create(CONFIG_NAME, mock(HttpClientConfiguration.class));
    assertThat(connectionManager.lookup(CONFIG_NAME).get(), is(sameInstance(client)));
  }

  @Test
  public void creatingAnExistingClientFails() {
    connectionManager.create(CONFIG_NAME, mock(HttpClientConfiguration.class));
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("There's an HttpClient available for config already.");
    connectionManager.create(CONFIG_NAME, mock(HttpClientConfiguration.class));
  }

  @Test
  public void sharedClientIsStartedByFirstUse() {
    HttpClientConfiguration configuration = getHttpClientConfiguration(CONFIG_NAME);
    ShareableHttpClient client1 = connectionManager.create(CONFIG_NAME, configuration);
    ShareableHttpClient client2 = connectionManager.lookup(CONFIG_NAME).get();
    client1.start();
    verify(delegateHttpClient).start();
    reset(delegateHttpClient);
    client2.start();
    verify(delegateHttpClient, never()).start();
  }

  @Test
  public void sharedClientIsStoppedByLastUse() {
    ShareableHttpClient client1 = connectionManager.create(CONFIG_NAME, getHttpClientConfiguration(CONFIG_NAME));
    ShareableHttpClient client2 = connectionManager.lookup(CONFIG_NAME).get();
    client1.start();
    client2.start();
    client1.stop();
    verify(delegateHttpClient, never()).stop();
    reset(delegateHttpClient);
    client2.stop();
    verify(delegateHttpClient).stop();
  }

  @Test
  public void differentClientsDoNotAffectEachOther() {
    ShareableHttpClient client1 = connectionManager.create(CONFIG_NAME, getHttpClientConfiguration(CONFIG_NAME));
    String otherConfig = "otherConfig";
    connectionManager.create(otherConfig, getHttpClientConfiguration(otherConfig));
    client1.start();
    verify(otherHttpClient, never()).start();
    client1.stop();
    verify(otherHttpClient, never()).stop();
  }

  @Test
  public void clientIsStartedAfterFirstError() {
    doThrow(Exception.class).doNothing().when(delegateHttpClient).start();
    ShareableHttpClient client = connectionManager.create(CONFIG_NAME, getHttpClientConfiguration(CONFIG_NAME));
    try {
      client.start();
    } catch (Exception e) {
      // Ignore first exception
    }
    client.start();
    verify(delegateHttpClient, Mockito.times(2)).start();
  }

  @Test
  @Issue("HTTPC-142")
  public void noExceptionIsRaisedWhenUsingLookupOrCreate() throws Exception {
    boolean someExceptionWasCaught = executeSeveralGetOrCreateConcurrentlyAndCheckIfAnExceptionOccurs(this::getOrCreateClient);
    assertThat(someExceptionWasCaught, is(false));
  }

  @Test
  @Issue("HTTPC-142")
  public void exceptionMayBeRaisedWhenUsingOldLookupAndCreateCombination() throws Exception {
    boolean someExceptionWasCaught = executeSeveralGetOrCreateConcurrentlyAndCheckIfAnExceptionOccurs(this::getOrCreateClientOld);
    assertThat(someExceptionWasCaught, is(true));
  }

  private boolean executeSeveralGetOrCreateConcurrentlyAndCheckIfAnExceptionOccurs(BiFunction<String, Supplier<? extends HttpClientConfiguration>, ShareableHttpClient> getOrCreateCallback)
      throws InterruptedException, ExecutionException {
    // Given a slow client factory.
    clientFactoryIsSlow.set(true);

    // When we try to getOrCreate clients concurrently
    int poolSize = 10;
    ExecutorService executorService = newFixedThreadPool(poolSize);
    AtomicBoolean someExceptionWasCaught = new AtomicBoolean(false);
    Collection<Future<?>> futures = new ArrayList<>(poolSize);
    for (int i = 0; i < poolSize; ++i) {
      futures.add(executorService.submit(() -> {
        try {
          getOrCreateCallback.apply(CONFIG_NAME, () -> getHttpClientConfiguration(CONFIG_NAME));
        } catch (IllegalArgumentException e) {
          LOGGER.error("Exception caught", e);
          someExceptionWasCaught.set(true);
        }
      }));
    }

    for (Future<?> future : futures) {
      future.get();
    }

    // The "then" part should be checked in the caller.
    return someExceptionWasCaught.get();
  }

  private ShareableHttpClient getOrCreateClientOld(String configName,
                                                   Supplier<? extends HttpClientConfiguration> configSupplier) {
    return connectionManager.lookup(configName).orElseGet(() -> connectionManager.create(configName, configSupplier.get()));
  }

  private ShareableHttpClient getOrCreateClient(String configName, Supplier<? extends HttpClientConfiguration> configSupplier) {
    return connectionManager.lookupOrCreate(configName, configSupplier);
  }

  private HttpClientConfiguration getHttpClientConfiguration(String configName) {
    HttpClientConfiguration configuration = mock(HttpClientConfiguration.class);
    when(configuration.getName()).thenReturn(configName);
    return configuration;
  }

}
