/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.message;

import org.mule.extension.http.internal.service.message.muletosdk.HttpEntityWrapper;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;
import org.mule.sdk.api.http.domain.entity.HttpEntity;
import org.mule.sdk.api.http.domain.entity.HttpEntityFactory;

import java.io.InputStream;
import java.nio.charset.Charset;

public class HttpEntityFactoryImpl implements HttpEntityFactory {

  @Override
  public HttpEntity fromByteArray(byte[] content) {
    return new HttpEntityWrapper(new ByteArrayHttpEntity(content));
  }

  @Override
  public HttpEntity fromString(String content, Charset charset) {
    return new HttpEntityWrapper(new ByteArrayHttpEntity(content.getBytes(charset)));
  }

  @Override
  public HttpEntity fromInputStream(InputStream content) {
    return new HttpEntityWrapper(new InputStreamHttpEntity(content));
  }

  @Override
  public HttpEntity fromInputStream(InputStream inputStream, Long contentLength) {
    return new HttpEntityWrapper(new InputStreamHttpEntity(inputStream, contentLength));
  }

  @Override
  public HttpEntity emptyEntity() {
    return new HttpEntityWrapper(new EmptyHttpEntity());
  }
}
