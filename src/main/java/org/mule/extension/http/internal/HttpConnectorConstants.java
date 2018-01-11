/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal;

import static com.google.common.collect.ImmutableSet.of;

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
  String DISABLE_RESPONSE_STREAMING_PROPERTY = "mule.http.disableResponseStreaming";
  Set<String> IDEMPOTENT_METHODS = of("GET", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE");

}
