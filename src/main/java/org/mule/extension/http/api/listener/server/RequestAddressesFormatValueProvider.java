/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.server;

import static org.mule.runtime.extension.api.values.ValueBuilder.getValuesFor;

import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueProvider;

import java.util.Set;

public class RequestAddressesFormatValueProvider implements ValueProvider {

  /**
   * Address has ip:port format, e.g., 127.0.0.1:8080
   */
  public static final String ADDRESS_ONLY = "Address only";

  /**
   * Address has hostname/ip:port format, e.g., host/127.0.0.1:8080
   */
  public static final String HOSTNAME_AND_ADDRESS = "Hostname and address";

  @Override
  public Set<Value> resolve() {
    return getValuesFor(ADDRESS_ONLY, HOSTNAME_AND_ADDRESS);
  }
}
