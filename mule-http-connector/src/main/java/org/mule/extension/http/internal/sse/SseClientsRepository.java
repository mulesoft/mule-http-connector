/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.sse;

import org.mule.sdk.api.http.sse.SseClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SseClientsRepository {

  private final Map<Long, SseClient> sseClients = new ConcurrentHashMap<>();

  public void addClient(SseClient sseClient) {
    sseClients.put(sseClient.getClientId(), sseClient);
  }

  public SseClient getClient(long id) {
    return sseClients.get(id);
  }
}
