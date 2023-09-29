/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.authentication;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.util.Objects;

/**
 * Base class for user/pass based implementations.
 *
 * @since 1.0
 */
public abstract class UsernamePasswordAuthentication implements HttpAuthentication, HttpRequestAuthentication {

  /**
   * The username to authenticate.
   */
  @Parameter
  private String username;

  /**
   * The password to authenticate.
   */
  @Parameter
  @Password
  private String password;

  /**
   * Configures if authentication should be preemptive or not. Preemptive authentication will send the authentication header in
   * the first request, instead of waiting for a 401 response code to send it.
   */
  @Parameter
  @Optional(defaultValue = "true")
  private boolean preemptive;

  @Override
  public void authenticate(HttpRequestBuilder builder) throws MuleException {
    // do nothing
  }

  @Override
  public boolean shouldRetry(Result<Object, HttpResponseAttributes> firstAttemptResult) throws MuleException {
    return false;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isPreemptive() {
    return preemptive;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UsernamePasswordAuthentication that = (UsernamePasswordAuthentication) o;
    return preemptive == that.preemptive &&
        Objects.equals(username, that.username) &&
        Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password, preemptive);
  }
}
