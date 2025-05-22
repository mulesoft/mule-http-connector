/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.message;

import org.mule.runtime.http.api.domain.message.response.HttpResponse;

public interface HttpResponseProxy {

  static HttpResponseProxy fromMuleApi(HttpResponse response) {
    return new HttpResponseProxyMuleApi(response);
  }

  org.mule.sdk.api.http.domain.message.response.HttpResponse toSdkApi();

  class HttpResponseProxyMuleApi implements HttpResponseProxy {

    private final HttpResponse response;

    HttpResponseProxyMuleApi(HttpResponse response) {
      this.response = response;
    }

    @Override
    public org.mule.sdk.api.http.domain.message.response.HttpResponse toSdkApi() {
      return new HttpResponseWrapper(response);
    }
  }
}
