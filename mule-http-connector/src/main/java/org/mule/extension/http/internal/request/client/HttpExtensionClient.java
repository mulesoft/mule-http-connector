/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.client;

import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static org.mule.runtime.http.api.client.auth.HttpAuthenticationType.BASIC;
import static org.mule.runtime.http.api.client.auth.HttpAuthenticationType.DIGEST;
import static org.mule.runtime.http.api.client.auth.HttpAuthenticationType.NTLM;

import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.internal.request.ShareableHttpClient;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.sdk.api.http.client.auth.HttpAuthenticationConfig;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.http.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Composition of a {@link ShareableHttpClient} with URI and authentication parameters that allow falling back to connection
 * default values for them.
 *
 * @since 1.0
 */
public class HttpExtensionClient implements Startable, Stoppable {

  private final HttpRequestAuthentication authentication;
  private final ShareableHttpClient httpClient;
  private final UriParameters uriParameters;

  public HttpExtensionClient(ShareableHttpClient httpClient, UriParameters uriParameters,
                             HttpRequestAuthentication authentication) {
    this.httpClient = httpClient;
    this.uriParameters = uriParameters;
    this.authentication = authentication;
  }

  /**
   * Returns the default parameters for the {@link HttpRequest} URI.
   */
  public UriParameters getDefaultUriParameters() {
    return uriParameters;
  }

  public HttpRequestAuthentication getDefaultAuthentication() {
    return authentication;
  }

  @Override
  public void start() throws MuleException {
    httpClient.start();
    try {
      startIfNeeded(authentication);
    } catch (Exception e) {
      httpClient.stop();
      throw e;
    }
  }

  @Override
  public void stop() throws MuleException {
    stopIfNeeded(authentication);
    httpClient.stop();
  }

  public CompletableFuture<HttpResponse> send(HttpRequest request, int responseTimeout, boolean followRedirects,
                                              HttpAuthentication authentication,
                                              HttpSendBodyMode sendBodyMode) {
    return httpClient.sendAsync(request, responseTimeout, followRedirects, authConfigurer(authentication), sendBodyMode);
  }

  private Consumer<HttpAuthenticationConfig> authConfigurer(HttpAuthentication authentication) {
    return configurer -> {
      if (authentication == null) {
        return;
      }

      if (NTLM.equals(authentication.getType())) {
        HttpAuthentication.HttpNtlmAuthentication ntlmAuthentication = (HttpAuthentication.HttpNtlmAuthentication) authentication;
        configurer.ntlm(ntlmAuthentication.getUsername(), ntlmAuthentication.getPassword(), ntlmAuthentication.isPreemptive(),
                        ntlmAuthentication.getDomain(), ntlmAuthentication.getWorkstation());
        return;
      }

      if (DIGEST.equals(authentication.getType())) {
        configurer.digest(authentication.getUsername(), authentication.getPassword(), authentication.isPreemptive());
        return;
      }

      if (BASIC.equals(authentication.getType())) {
        configurer.basic(authentication.getUsername(), authentication.getPassword(), authentication.isPreemptive());
      }
    };
  }
}
