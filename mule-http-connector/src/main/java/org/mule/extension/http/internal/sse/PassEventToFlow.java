/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.sse;

import org.mule.extension.http.api.SseEventAttributes;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.mule.sdk.api.http.sse.ServerSentEvent;
import org.mule.sdk.api.http.sse.ServerSentEventListener;

public class PassEventToFlow implements ServerSentEventListener {

  private final SourceCallback<String, SseEventAttributes> sourceCallback;

  public PassEventToFlow(SourceCallback<String, SseEventAttributes> sourceCallback) {
    this.sourceCallback = sourceCallback;
  }

  @Override
  public void onEvent(ServerSentEvent event) {
    SourceCallbackContext sourceContext = sourceCallback.createContext();
    sourceCallback.handle(Result.<String, SseEventAttributes>builder()
        .output(event.getEventData())
        .attributes(getAttributes(event))
        .build(), sourceContext);
  }

  private SseEventAttributes getAttributes(ServerSentEvent event) {
    return new SseEventAttributes(event);
  }
}
