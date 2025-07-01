/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.proxy;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * An object that handles proxy authentication and execution for HTTP requests.
 * <p>
 * For dynamic configurations of the {@code http:request-config} but with a static {@code http:proxy}, the instances of this
 * interface implementors will be shared. This is relevant for implementations that also implement {@link Lifecycle} or any of its
 * superinterfaces, since the lifecycle methods will be tied to the lifecycle of the dynamic connection.
 *
 * @since 1.0
 */
public interface HttpRequestProxyAuthentication {

  static final Logger LOGGER = getLogger(HttpRequestProxyAuthentication.class);

  /**
   * Executes an HTTP request through the proxy with authentication. This method will be called by the HTTP connector to delegate
   * request execution to the proxy implementation.
   *
   * @param request         The HTTP request to execute
   * @param responseTimeout The timeout for the response in milliseconds
   * @param followRedirects Whether to follow redirects
   * @param sendBodyMode    The mode for sending the request body
   * @return A CompletableFuture that will complete with the HTTP response
   * @throws MuleException if the request execution fails
   */
  CompletableFuture<HttpResponse> executeRequest(HttpRequest request, int responseTimeout,
                                                 boolean followRedirects,
                                                 org.mule.extension.http.api.request.HttpSendBodyMode sendBodyMode)
      throws MuleException;

  /**
   * Detects if there was an authentication failure in the response. After sending an HTTP request and creating a result with the
   * response, this method will be executed.
   * <p>
   * If it returns false, the caller must continue executing. If it returns true, the caller will try to send the request again.
   *
   * @param firstAttemptResult The result with the response of the request.
   * @return True if the request should be sent again, false otherwise.
   *
   * @deprecated implement {@link #retryIfShould(Result, Runnable, Runnable)} that allows to do a non-blocking retry.
   */
  @Deprecated
  default boolean shouldRetry(Result<Object, HttpResponseAttributes> firstAttemptResult) throws MuleException {
    return false;
  }

  /**
   * Detects if there was an authentication failure in the response. After sending an HTTP request and creating a result with the
   * response, this method will be executed.
   * <p>
   * If an authentication failure is detected, the the provided {@code retryCallback} will be called. If not,
   * {@code notRetryCallback} will be called instead.
   *
   * @param firstAttemptResult The result with the response of the request.
   * @param retryCallback      the callback that performs the retry of the request.
   * @param notRetryCallback   the callback that performs any necessary steps for not retrying the request.
   */
  default void retryIfShould(Result<Object, HttpResponseAttributes> firstAttemptResult, Runnable retryCallback,
                             Runnable notRetryCallback) {
    try {
      if (shouldRetry(firstAttemptResult)) {
        retryCallback.run();
      } else {
        notRetryCallback.run();
      }
    } catch (MuleException e) {
      LOGGER.error("Exception caught in non-blocking HTTP retry for proxy authentication.", e);
      notRetryCallback.run();
    }
  }

  /**
   * Indicates if the response body may be read in the process of determining the result of the authenticated request
   *
   * @return Whether the response body may be read or not
   * @since 1.6, 1.5.20
   */
  default boolean readsAuthenticatedResponseBody() {
    return false;
  }
}
