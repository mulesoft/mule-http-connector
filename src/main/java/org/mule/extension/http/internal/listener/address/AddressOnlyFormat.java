/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.address;

import org.mule.runtime.http.api.domain.request.ClientConnection;
import org.mule.runtime.http.api.domain.request.ServerConnection;

public class AddressOnlyFormat implements RequestAddressesFormat {

  @Override
  public String localAddress(ServerConnection serverConnection) {
    return serverConnection.getLocalHostAddress().getAddress().getHostAddress().concat(":")
        .concat(Integer.toString(serverConnection.getLocalHostAddress().getPort()));
  }

  @Override
  public String remoteAddress(ClientConnection clientConnection) {
    return clientConnection.getRemoteHostAddress().getHostString().concat(":")
        .concat(Integer.toString(clientConnection.getRemoteHostAddress().getPort()));
  }
}
