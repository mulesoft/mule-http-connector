/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.authentication;

import static org.mule.runtime.http.api.client.auth.HttpAuthenticationType.DIGEST;
import org.mule.runtime.http.api.client.auth.HttpAuthenticationType;

/**
 * Configures digest authentication for the requests.
 *
 * @since 1.0
 */
public class DigestAuthentication extends UsernamePasswordAuthentication {

  @Override
  public HttpAuthenticationType getType() {
    return DIGEST;
  }

}
