/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

import java.io.IOException;

/**
 * Wrapper for a {@link CursorProvider} that returns cursors with some offset.
 */
final class OffsetCursorProviderWrapper implements CursorStreamProvider {

  private final long offset;
  private final CursorStreamProvider provider;

  public OffsetCursorProviderWrapper(CursorStreamProvider provider, long offset) {
    this.offset = offset;
    this.provider = provider;
  }

  @Override
  public CursorStream openCursor() {
    CursorStream cursor = provider.openCursor();
    try {
      cursor.seek(offset);
    } catch (IOException e) {
      throw new MuleRuntimeException(e);
    }
    return cursor;
  }

  @Override
  public void close() {
    provider.close();
  }

  @Override
  public void releaseResources() {
    provider.releaseResources();
  }

  @Override
  public boolean isClosed() {
    return provider.isClosed();
  }
}
