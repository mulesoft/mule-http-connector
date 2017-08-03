/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting;

import org.mule.runtime.api.util.MultiMap;

import java.util.Map;

/**
 * Null object pattern for {@link Interception}
 */
public class NoInterception implements Interception {

  @Override
  public Map<String, String> getHeaders() {
    return new MultiMap<>();
  }
}
