/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.sse;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.sdk.api.http.sse.SseClient;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

public class SseOperations {

  private static final Logger LOGGER = getLogger(SseOperations.class);

  @Inject
  @Named("_sseClientsRepository")
  private SseClientsRepository sseClientsRepository;

  // TODO: inject -> param
  // TODO: other params
  // TODO: client not found
  // TODO: close client

  public void sendSseEvent(long clientId, String eventName, String data, @Optional String id, @Optional Long retry)
      throws IOException {
    LOGGER.trace("SSE event {} received for client {}", eventName, clientId);
    SseClient sseClient = sseClientsRepository.getClient(clientId);
    sseClient.sendEvent(eventName, data);
  }
}
