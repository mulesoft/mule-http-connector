/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.message.sdktomule;

import static java.util.stream.Collectors.toList;

import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public class HttpEntityWrapper implements HttpEntity {

  private final org.mule.sdk.api.http.domain.entity.HttpEntity sdkEntity;

  public HttpEntityWrapper(org.mule.sdk.api.http.domain.entity.HttpEntity sdkEntity) {
    this.sdkEntity = sdkEntity;
  }

  @Override
  public boolean isStreaming() {
    return sdkEntity.isStreaming();
  }

  @Override
  public boolean isComposed() {
    return sdkEntity.isComposed();
  }

  @Override
  public InputStream getContent() {
    return sdkEntity.getContent();
  }

  @Override
  public byte[] getBytes() throws IOException {
    return sdkEntity.getBytes();
  }

  @Override
  public Collection<HttpPart> getParts() throws IOException {
    return sdkEntity.getParts().stream().map(HttpPartWrapper::new).collect(toList());
  }

  @Override
  public Optional<Long> getLength() {
    return sdkEntity.getLength();
  }
}
