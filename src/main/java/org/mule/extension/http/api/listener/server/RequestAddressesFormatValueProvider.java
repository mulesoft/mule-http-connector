/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.server;

import static com.google.common.collect.Sets.newHashSet;
import static org.mule.runtime.extension.api.values.ValueBuilder.newValue;

import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;

import java.util.Set;

public class RequestAddressesFormatValueProvider implements ValueProvider {

  /**
   * Address has ip:port format, e.g., 127.0.0.1:8080
   */
  public static final String ADDRESS_ONLY = "ADDRESS_ONLY";

  /**
   * Address has hostname/ip:port format, e.g., host/127.0.0.1:8080
   */
  public static final String HOSTNAME_AND_ADDRESS = "HOSTNAME_AND_ADDRESS";

  @Override
  public Set<Value> resolve() {
    return newHashSet(
                      newValue(ADDRESS_ONLY).withDisplayName("Address only").build(),
                      newValue(HOSTNAME_AND_ADDRESS).withDisplayName("Hostname and address").build());
  }
}
