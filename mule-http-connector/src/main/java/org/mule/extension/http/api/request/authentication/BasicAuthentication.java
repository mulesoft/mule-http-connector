/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.authentication;

import static org.mule.runtime.http.api.client.auth.HttpAuthenticationType.BASIC;
import org.mule.runtime.http.api.client.auth.HttpAuthenticationType;


/**
 * Configures basic authentication for the requests.
 *
 * @since 1.0
 */
public class BasicAuthentication extends UsernamePasswordAuthentication {

  @Override
  public HttpAuthenticationType getType() {
    return BASIC;
  }

}
