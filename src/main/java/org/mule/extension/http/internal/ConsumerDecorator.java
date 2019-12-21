/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal;

import org.slf4j.MDC;

import java.util.Map;
import java.util.function.BiConsumer;

public class ConsumerDecorator {

  public static <T, U> BiConsumer<T, U> withMdc(BiConsumer<T, U> consumer) {
    Map<String, String> mdc = MDC.getCopyOfContextMap();
    return (T t, U u) -> {
      try {
        MDC.setContextMap(mdc);
        consumer.accept(t, u);
      } finally {
        MDC.clear();
      }
    };
  }
}
