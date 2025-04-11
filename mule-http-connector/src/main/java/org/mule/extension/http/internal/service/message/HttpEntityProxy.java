/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.service.message;

import org.mule.sdk.api.http.domain.entity.HttpEntity;

import java.io.InputStream;
import java.util.OptionalLong;

public interface HttpEntityProxy {

  static HttpEntityProxy forSdkApi(HttpEntity entity) {
    return null;
  }

  static HttpEntityProxy forMuleApi(org.mule.runtime.http.api.domain.entity.HttpEntity entity) {
    return null;
  }

  InputStream getContent();

  OptionalLong getLength();
}
