/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import org.mule.runtime.http.api.server.RequestHandlerManager;
import org.mule.sdk.api.http.server.EndpointAvailabilityHandler;

/**
 * Wrapper for {@link EndpointAvailabilityHandler} or {@link RequestHandlerManager}, that represent both the same thing: an object
 * to activate or deactivate an endpoint.
 */
public interface EndpointAvailabilityManager {

  static EndpointAvailabilityManager forSdkApi(EndpointAvailabilityHandler delegate) {
    return new ImplementationForSdkApi(delegate);
  }

  static EndpointAvailabilityManager forMuleApi(RequestHandlerManager delegate) {
    return new ImplementationForMuleApi(delegate);
  }

  /**
   * Temporarily stops the handler from being accessed, resulting in a 503 status code return by the server.
   */
  void unavailable();

  /**
   * Allows access to the handler.
   */
  void available();

  /**
   * Removes the handler from the server (generating a 404 if no other endpoint handles the corresponding path).
   */
  void remove();

  class ImplementationForSdkApi implements EndpointAvailabilityManager {

    private final EndpointAvailabilityHandler delegate;

    ImplementationForSdkApi(EndpointAvailabilityHandler delegate) {
      this.delegate = delegate;
    }

    @Override
    public void unavailable() {
      delegate.unavailable();
    }

    @Override
    public void available() {
      delegate.available();
    }

    @Override
    public void remove() {
      delegate.remove();
    }
  }

  class ImplementationForMuleApi implements EndpointAvailabilityManager {

    private final RequestHandlerManager delegate;

    ImplementationForMuleApi(RequestHandlerManager delegate) {
      this.delegate = delegate;
    }

    @Override
    public void unavailable() {
      delegate.stop();
    }

    @Override
    public void available() {
      delegate.start();
    }

    @Override
    public void remove() {
      delegate.dispose();
    }
  }
}
