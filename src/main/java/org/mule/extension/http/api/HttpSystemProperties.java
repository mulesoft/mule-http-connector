/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import org.mule.extension.http.api.listener.HttpBasicAuthenticationFilter;

public class HttpSystemProperties {

  private HttpSystemProperties() {

  }

  public static void refresh() {
    HttpBasicAuthenticationFilter.refreshSystemProperties();
    HttpMessageBuilder.refreshSystemProperties();
  }

}
