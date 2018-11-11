/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.authentication;

import static org.mule.runtime.http.api.client.auth.HttpAuthenticationType.NTLM;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.http.api.client.auth.HttpAuthentication.HttpNtlmAuthentication;
import org.mule.runtime.http.api.client.auth.HttpAuthenticationType;

import java.util.Objects;

/**
 * Configures NTLM authentication for the requests.
 *
 * @since 1.0
 */
public class NtlmAuthentication extends UsernamePasswordAuthentication implements HttpNtlmAuthentication {

  /**
   * The domain to authenticate.
   */
  @Parameter
  @Optional
  private String domain;

  /**
   * The workstation to authenticate.
   */
  @Parameter
  @Optional
  private String workstation;

  @Override
  public HttpAuthenticationType getType() {
    return NTLM;
  }

  @Override
  public String getDomain() {
    return domain;
  }

  @Override
  public String getWorkstation() {
    return workstation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    NtlmAuthentication that = (NtlmAuthentication) o;
    return Objects.equals(domain, that.domain) &&
        Objects.equals(workstation, that.workstation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), domain, workstation);
  }
}
