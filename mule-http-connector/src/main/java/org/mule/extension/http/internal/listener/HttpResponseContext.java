/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import org.mule.extension.http.internal.listener.intercepting.Interception;
import org.mule.extension.http.internal.listener.intercepting.NoInterception;
import org.mule.extension.http.internal.ser.HttpResponseReadyCallbackProxy;
import org.mule.runtime.http.api.server.async.HttpResponseReadyCallback;

/**
 * Holds temporary state necessary to emit an http response
 *
 * @since 1.0
 */
public class HttpResponseContext {

  private String httpVersion;
  private boolean supportStreaming = true;
  private HttpResponseReadyCallbackProxy responseCallback;
  private Interception interception;
  private boolean deferredResponse = false;

  public String getHttpVersion() {
    return httpVersion;
  }

  public void setHttpVersion(String httpVersion) {
    this.httpVersion = httpVersion;
  }

  public boolean isSupportStreaming() {
    return supportStreaming;
  }

  public void setSupportStreaming(boolean supportStreaming) {
    this.supportStreaming = supportStreaming;
  }

  public HttpResponseReadyCallbackProxy getResponseCallback() {
    return responseCallback;
  }

  public void setResponseCallback(HttpResponseReadyCallbackProxy responseCallback) {
    this.responseCallback = responseCallback;
  }

  public void setInterception(Interception interception) {
    this.interception = interception;
  }

  public Interception getInterception() {
    return interception != null ? interception : new NoInterception();
  }

  public boolean isDeferredResponse() {
    return deferredResponse;
  }

  public void setDeferredResponse(boolean deferredResponse) {
    this.deferredResponse = deferredResponse;
  }
}
