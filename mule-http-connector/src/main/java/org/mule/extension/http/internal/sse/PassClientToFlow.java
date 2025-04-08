/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.sse;

import org.mule.extension.http.api.SseClientAttributes;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.mule.sdk.api.http.sse.SseClient;

import java.util.function.Consumer;

public class PassClientToFlow implements Consumer<SseClient> {

  private final SourceCallback<Void, SseClientAttributes> sourceCallback;
  private final SseClientsRepository sseClientsRepository;

  public PassClientToFlow(SourceCallback<Void, SseClientAttributes> sourceCallback, SseClientsRepository sseClientsRepository) {
    this.sourceCallback = sourceCallback;
    this.sseClientsRepository = sseClientsRepository;
  }

  @Override
  public void accept(SseClient sseClient) {
    sseClientsRepository.addClient(sseClient);

    SourceCallbackContext sourceContext = sourceCallback.createContext();
    sourceCallback.handle(Result.<Void, SseClientAttributes>builder()
        .output(null)
        .attributes(getAttributes(sseClient))
        .build(), sourceContext);
  }

  private SseClientAttributes getAttributes(SseClient sseClient) {
    return new SseClientAttributes(sseClient.getClientId());
  }
}
