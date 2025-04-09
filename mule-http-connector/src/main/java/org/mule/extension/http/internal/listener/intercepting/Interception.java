/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting;

import org.mule.runtime.api.util.MultiMap;

/**
 * Defines the protocol an interception of a resquest must contain.
 */
public interface Interception {

  /**
   * Returns the headers used for response decoration, deny-list or other operation defined by the interceptor.
   *
   * @return headers to operate with.
   */
  MultiMap<String, String> getHeaders();
}
