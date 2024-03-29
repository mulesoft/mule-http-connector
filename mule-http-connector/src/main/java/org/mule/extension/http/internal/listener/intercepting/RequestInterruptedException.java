/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;

/**
 * Exception that suggest request to be interrupted and flow not executed.
 */
public class RequestInterruptedException extends InterceptingException {

  public RequestInterruptedException(HttpStatus status, MultiMap<String, String> headers) {
    super(status, headers);
  }
}
