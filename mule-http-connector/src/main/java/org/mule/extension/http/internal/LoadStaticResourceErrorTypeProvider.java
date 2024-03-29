/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal;

import static java.util.Collections.singleton;
import static org.mule.extension.http.api.error.HttpError.NOT_FOUND;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Set;

/**
 * Errors that can be thrown in the {@link HttpOperations#loadStaticResource(StaticResourceLoader)} operation.
 *
 * @since 1.0
 */
public class LoadStaticResourceErrorTypeProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return singleton(NOT_FOUND);
  }
}
