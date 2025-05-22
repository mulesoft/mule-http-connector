/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import org.mule.extension.http.internal.service.message.HttpResponseProxy;
import org.mule.sdk.api.http.server.async.HttpResponseReadyCallback;

public interface HttpResponseReadyCallbackProxy {

  static HttpResponseReadyCallbackProxy forSdkApi(HttpResponseReadyCallback readyCallback) {
    return new ImplementationForSdkApi(readyCallback);
  }

  static HttpResponseReadyCallbackProxy forMuleApi() {
    return new ImplementationForMuleApi();
  }

  void responseReady(HttpResponseProxy httpResponse, ResponseStatusCallbackProxy failureResponseStatusCallback);


  class ImplementationForSdkApi implements HttpResponseReadyCallbackProxy {

    private final HttpResponseReadyCallback readyCallback;

    public ImplementationForSdkApi(HttpResponseReadyCallback readyCallback) {
      this.readyCallback = readyCallback;
    }

    @Override
    public void responseReady(HttpResponseProxy httpResponse, ResponseStatusCallbackProxy statusCallback) {
      readyCallback.responseReady(httpResponse.toSdkApi(), ResponseStatusCallbackProxy.forSdkApi(statusCallback));
    }
  }


  class ImplementationForMuleApi implements HttpResponseReadyCallbackProxy {

    @Override
    public void responseReady(HttpResponseProxy httpResponse, ResponseStatusCallbackProxy failureResponseStatusCallback) {

    }
  }
}
