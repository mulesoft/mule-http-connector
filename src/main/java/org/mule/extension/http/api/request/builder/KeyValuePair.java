/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

/**
 * A single two-strings pair used for key-values used in contexts where using expressions for building maps doesn't
 * make sense, as within the connection providers.
 *
 * @since 1.7
 */
public interface KeyValuePair {

  String getKey();

  String getValue();
}
