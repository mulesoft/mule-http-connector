/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.extension.http.api.request.builder.KeyValuePair;
import org.mule.runtime.api.util.MultiMap;


public class KeyValuePairUtils {

  private KeyValuePairUtils() {
    // Empty private constructor to avoid instantiation.
  }

  /**
   * Returns a MultiMap representation of an iterable (e.g. a List) of KeyValuePairs
   * @param iterable
   * @return
   */
  public static MultiMap<String, String> toMultiMap(Iterable<? extends KeyValuePair> iterable) {
    MultiMap<String, String> asMultiMap = new MultiMap<>();
    iterable.forEach(pair -> asMultiMap.put(pair.getKey(), pair.getValue()));
    return asMultiMap;
  }
}
