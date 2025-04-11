/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import org.mule.runtime.http.api.domain.message.response.HttpResponse;

public interface HttpResponseReadyCallbackProxy {

  void responseReady(HttpResponse httpResponse, ResponseStatusCallbackProxy failureResponseStatusCallback);
}
