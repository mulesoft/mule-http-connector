/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal;

import static org.mule.extension.http.api.error.HttpError.BASIC_AUTHENTICATION;
import static org.mule.extension.http.api.error.HttpError.SERVER_SECURITY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.extension.http.api.error.ResourceNotFoundException;
import org.mule.extension.http.api.listener.HttpBasicAuthenticationFilter;
import org.mule.extension.http.internal.filter.BasicUnauthorisedException;
import org.mule.runtime.api.security.SecurityException;
import org.mule.runtime.api.security.SecurityProviderNotFoundException;
import org.mule.runtime.api.security.UnknownAuthenticationTypeException;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.security.AuthenticationHandler;

/**
 * General HTTP operations that do not required any specific configuration or connection.
 *
 * @since 1.0
 */
public class HttpOperations {

  /**
   * Authenticates received HTTP requests. Must be used after a listener component.
   */
  @Throws(BasicSecurityErrorTypeProvider.class)
  public void basicSecurityFilter(@ParameterGroup(name = "Security Filter") HttpBasicAuthenticationFilter filter,
                                  AuthenticationHandler authenticationHandler) {
    try {
      filter.authenticate(authenticationHandler);
    } catch (BasicUnauthorisedException e) {
      throw new ModuleException(BASIC_AUTHENTICATION, e);
    } catch (SecurityProviderNotFoundException | SecurityException | UnknownAuthenticationTypeException e) {
      throw new ModuleException(SERVER_SECURITY, e);
    }
  }

  /**
   * Serves up static content for use with HTTP, using the request path to lookup the resource.
   *
   * @return the resource defined by the path of an HTTP request
   */
  @MediaType(value = ANY, strict = false)
  @Throws(LoadStaticResourceErrorTypeProvider.class)
  public Result<?, ?> loadStaticResource(@ParameterGroup(name = "Resource") StaticResourceLoader resourceLoader)
      throws ResourceNotFoundException {
    return resourceLoader.load();
  }

}
