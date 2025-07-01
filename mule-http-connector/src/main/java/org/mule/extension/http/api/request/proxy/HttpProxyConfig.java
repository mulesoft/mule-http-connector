/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.proxy;

import org.mule.runtime.http.api.client.proxy.ProxyConfig;

/**
 * Marker interface for exposing the proxy configuration as an imported type. Extends HttpRequestProxyAuthentication to allow
 * proxy implementations to provide custom execution behavior.
 */
public interface HttpProxyConfig extends ProxyConfig, HttpRequestProxyAuthentication {

  interface HttpNtlmProxyConfig extends HttpProxyConfig, NtlmProxyConfig {
  }
}
