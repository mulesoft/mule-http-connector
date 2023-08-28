/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.sdk.api.runtime.source.DistributedTraceContextManager;

import static java.util.Collections.emptyMap;

import java.util.Map;

/**
 * A {@link DistributedTraceContextManager} that provides an empty trace context map.
 *
 * @since 1.8.0
 */
public class EmptyDistributedTraceContextManager implements DistributedTraceContextManager {

  private static final DistributedTraceContextManager INSTANCE = new EmptyDistributedTraceContextManager();

  public static final DistributedTraceContextManager getDistributedTraceContextManager() {
    return INSTANCE;
  }

  private EmptyDistributedTraceContextManager() {

  }

  @Override
  public void setRemoteTraceContextMap(Map<String, String> contextMap) {
    // Nothing to do.
  }

  @Override
  public Map<String, String> getRemoteTraceContextMap() {
    return emptyMap();
  }

  @Override
  public void setCurrentSpanName(String name) {
    // TODO: W-10876465 A.8.2 Respect semantic conventions of span generation in HTTP
    // Nothing to do.
  }

  @Override
  public void addCurrentSpanAttribute(String key, String value) {
    // TODO: W-10876465 A.8.2 Respect semantic conventions of span generation in HTTP
    // Nothing to do.
  }

  @Override
  public void addCurrentSpanAttributes(Map<String, String> attributes) {
    // TODO: W-10876465 A.8.2 Respect semantic conventions of span generation in HTTP
    // Nothing to do.
  }
}
