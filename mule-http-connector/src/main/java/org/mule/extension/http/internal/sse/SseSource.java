/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.sse;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.sdk.api.http.sse.SseRetryConfig.DEFAULT;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.api.SseEventAttributes;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.sdk.api.http.sse.ServerSentEventSource;

import org.slf4j.Logger;

@Alias("sseSource")
@MediaType(value = ANY, strict = false)
public class SseSource extends Source<String, SseEventAttributes> {

  private static final Logger LOGGER = getLogger(SseSource.class);

  @Connection
  private ConnectionProvider<HttpExtensionClient> clientProvider;

  @Parameter
  @Placement(order = 1)
  @Optional(defaultValue = "/")
  private String path = "/";

  /**
   * Event name to listen. Defaults to "*", which means that it's a fallback for all non-listened event names.
   */
  @Parameter
  @Placement(order = 2)
  @Optional(defaultValue = "*")
  private String eventName = "*";

  private ServerSentEventSource serverSentEventSource;

  @Override
  public void onStart(SourceCallback<String, SseEventAttributes> sourceCallback) throws MuleException {
    HttpExtensionClient client = clientProvider.connect();
    serverSentEventSource = client.sseSource(path, DEFAULT);
    if ("*".equals(eventName)) {
      serverSentEventSource.register(new PassEventToFlow(sourceCallback));
    } else {
      serverSentEventSource.register(eventName, new PassEventToFlow(sourceCallback));
    }
    serverSentEventSource.open();
  }

  @Override
  public void onStop() {
    // serverSentEventSource.stop();
  }
}
