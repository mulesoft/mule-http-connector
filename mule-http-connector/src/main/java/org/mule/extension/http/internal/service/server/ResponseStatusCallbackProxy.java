/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

// TODO: Replace this with a CompletableFuture<Void>?
public interface ResponseStatusCallbackProxy {

  static org.mule.sdk.api.http.server.async.ResponseStatusCallback forSdkApi(ResponseStatusCallbackProxy statusCallback) {
    return new org.mule.sdk.api.http.server.async.ResponseStatusCallback() {

      @Override
      public void responseSendFailure(Throwable throwable) {
        statusCallback.responseSendFailure(throwable);
      }

      @Override
      public void responseSendSuccessfully() {
        statusCallback.responseSendSuccessfully();
      }
    };
  }

  void responseSendFailure(Throwable throwable);

  void responseSendSuccessfully();
}
