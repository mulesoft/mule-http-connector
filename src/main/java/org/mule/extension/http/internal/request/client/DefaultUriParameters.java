/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.client;

import static org.slf4j.LoggerFactory.getLogger;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.runtime.http.api.HttpConstants.Protocol;

import org.slf4j.Logger;

/**
 * Default implementation of {@link UriParameters}.
 *
 * @since 1.0
 */
public class DefaultUriParameters implements UriParameters {

  private static final Logger LOGGER = getLogger(DefaultUriParameters.class);

  private final Protocol scheme;
  private final String host;
  private final Integer port;


  public DefaultUriParameters(Protocol protocol, String host, Integer port) {
    this.scheme = protocol;
    this.host = host;
    this.port = getValidPort(protocol, host, port);
  }

  @Override
  public Protocol getScheme() {
    return scheme;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  private int getValidPort(Protocol protocol, String host, Integer configuredPort) {
    int defaultProtocolPort = protocol.getDefaultPort();
    if (configuredPort == null) {
      return defaultProtocolPort;
    } else if (configuredPort < 0) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("Invalid port: " + configuredPort + " for host " + host + ", defaulting to " + defaultProtocolPort);
      }
      return defaultProtocolPort;
    }
    return configuredPort;
  }
}
