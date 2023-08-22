/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting;

import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;

import org.mule.runtime.api.util.MultiMap;

/**
 * Null object pattern for {@link Interception}
 */
public class NoInterception implements Interception {

  @Override
  public MultiMap<String, String> getHeaders() {
    return emptyMultiMap();
  }
}
