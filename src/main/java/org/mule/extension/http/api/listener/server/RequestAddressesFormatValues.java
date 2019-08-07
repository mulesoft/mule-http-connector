/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.server;

public enum RequestAddressesFormatValues {

  /**
   * Address has ip:port format, e.g., 127.0.0.1:8080
   */
  ADDRESS_ONLY,

  /**
   * Address has hostname/ip:port format, e.g., host/127.0.0.1:8080
   */
  HOSTNAME_AND_ADDRESS;
}
