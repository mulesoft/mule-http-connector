/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

/**
 * A single two-strings pair used for key-values used in contexts where using expressions for building maps doesn't make sense, as
 * within the connection providers.
 *
 * @since 1.8
 */
public interface KeyValuePair {

  /**
   * @return the key of the {@link KeyValuePair}, that could be use as a key for a {@link java.util.Map}
   */
  String getKey();

  /**
   * @return the value of the {@link KeyValuePair}
   */
  String getValue();

}
