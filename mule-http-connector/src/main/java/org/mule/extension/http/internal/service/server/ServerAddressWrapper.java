/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.server;

import org.mule.runtime.http.api.server.ServerAddress;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ServerAddressWrapper implements org.mule.sdk.api.http.server.ServerAddress {

  private final ServerAddress delegate;

  public ServerAddressWrapper(ServerAddress delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getPort() {
    return delegate.getPort();
  }

  @Override
  public String getIp() {
    return delegate.getIp();
  }

  @Override
  public InetAddress getAddress() {
    return new InetSocketAddress(delegate.getIp(), delegate.getPort()).getAddress();
  }

  @Override
  public boolean overlaps(org.mule.sdk.api.http.server.ServerAddress serverAddress) {
    if (serverAddress instanceof ServerAddressWrapper) {
      ServerAddressWrapper otherWrapper = (ServerAddressWrapper) serverAddress;
      return delegate.overlaps(otherWrapper.delegate);
    } else {
      throw new IllegalArgumentException("Comparing pears with apples!");
    }
  }
}
