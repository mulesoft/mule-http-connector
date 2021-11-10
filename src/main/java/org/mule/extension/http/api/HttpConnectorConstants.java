/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;

import java.util.HashSet;
import java.util.Set;

/**
 * Constants of the HTTP Connector.
 *
 * @since 1.0
 */
public interface HttpConnectorConstants {

  String URL_CONFIGURATION = "URL Configuration";
  String RESPONSE = "Response";
  String REQUEST = "Request";
  String RESPONSES = "Responses";
  String CONNECTOR_OVERRIDES = "Connector Overrides";
  String TLS_CONFIGURATION = "TLS Configuration";
  String AUTHENTICATION = "Authentication";
  Set<String> IDEMPOTENT_METHODS = unmodifiableSet(new HashSet<>(asList("GET", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE")));
  String REMOTELY_CLOSED = "Remotely closed";
  String RETRY_ATTEMPTS_PROPERTY = SYSTEM_PROPERTY_PREFIX + "http.client.maxRetries";
  String RETRY_ON_ALL_METHODS_PROPERTY = SYSTEM_PROPERTY_PREFIX + "http.client.retryOnAllMethods";
  String ENCODE_URI_PARAMS_PROPERTY = SYSTEM_PROPERTY_PREFIX + "http.client.encodeUriParams";
  String BASIC_LAX_DECODING_PROPERTY = SYSTEM_PROPERTY_PREFIX + "http.basic.laxBase64Decoding";
  String HTTP_ENABLE_PROFILING = SYSTEM_PROPERTY_PREFIX + "http.profiling.enable";
  int DEFAULT_RETRY_ATTEMPTS = 3;

}
