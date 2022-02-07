/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Inject;

/**
 * Manages {@link ShareableHttpClient ShareableHttpClients} across multiple configurations based on their name, meaning two
 * configurations spawning from the same prototype will receive the same {@link ShareableHttpClient}.
 *
 * @since 1.0
 */
public class HttpRequesterConnectionManager implements Disposable {

  @Inject
  private HttpService httpService;

  private Map<String, ShareableHttpClient> clients = new HashMap<>();

  public HttpRequesterConnectionManager() {}

  public HttpRequesterConnectionManager(HttpService httpService) {
    this.httpService = httpService;
  }

  /**
   * Searches for an already existing {@link ShareableHttpClient} associated with the desired configuration name.
   *
   * @param configName the name of the client to look for
   * @return an {@link Optional} with an {@link ShareableHttpClient} if found or an empty one otherwise
   * @deprecated use {@link #lookupOrCreate} instead.
   */
  @Deprecated
  public Optional<ShareableHttpClient> lookup(String configName) {
    return ofNullable(clients.get(configName));
  }

  /**
   * Creates an {@link ShareableHttpClient} associated with the given configuration name. If there's already one, this operation
   * will fail so {@link #lookup(String)} should be used first.
   *
   * @param configName
   * @param clientConfiguration
   * @return
   * @deprecated use {@link #lookupOrCreate} instead.
   */
  @Deprecated
  public synchronized ShareableHttpClient create(String configName, HttpClientConfiguration clientConfiguration) {
    checkArgument(!clients.containsKey(configName), format("There's an HttpClient available for %s already.", configName));
    ShareableHttpClient client = new ShareableHttpClient(httpService.getClientFactory().create(clientConfiguration));
    clients.put(configName, client);
    return client;
  }

  /**
   * Searches for an already existing {@link ShareableHttpClient} associated with the desired configuration name.
   * If there isn't a {@link ShareableHttpClient} present, it creates an {@link ShareableHttpClient}.
   *
   * @param configName the name of the client to look for.
   * @param configSupplier a supplier from {@link HttpClientConfiguration}. It's only utilised if a new {@link ShareableHttpClient} is created.
   * @return the corresponding {@link ShareableHttpClient} if found or a new {@link ShareableHttpClient} otherwise.
   */
  public synchronized ShareableHttpClient lookupOrCreate(String configName,
                                                         Supplier<? extends HttpClientConfiguration> configSupplier) {
    return clients.computeIfAbsent(configName,
                                   name -> new ShareableHttpClient(httpService.getClientFactory().create(configSupplier.get())));
  }

  @Override
  public void dispose() {
    clients.clear();
  }

  /**
   * Removes from the internal cache the {@link ShareableHttpClient} associated with a given {@code configName}
   *
   * @param configName
   *
   * @since 1.5.21
   */
  public void disposeClient(String configName) {
    clients.remove(configName);
  }


}
