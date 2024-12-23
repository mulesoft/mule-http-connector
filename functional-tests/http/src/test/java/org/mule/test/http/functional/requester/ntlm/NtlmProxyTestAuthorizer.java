/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.ntlm;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import org.mule.runtime.core.api.util.Base64;
import org.mule.test.http.functional.requester.TestAuthorizer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NtlmProxyTestAuthorizer implements TestAuthorizer {

  private static final String TYPE_2_MESSAGE_CHALLENGE = "TlRMTVNTUAACAAAAAAAAACgAAAABggAAU3J2Tm9uY2UAAAAAAAAAAA==";
  private static final String TYPE_2_MESSAGE = "NTLM " + TYPE_2_MESSAGE_CHALLENGE;
  private static final String AUTHORIZED = "Authorized";

  private String clientAuthHeader;
  private String serverAuthHeader;
  private int unauthorizedHeader;
  private String user;
  private String password;
  private String domain;
  private String workstation;


  public NtlmProxyTestAuthorizer(String clientAuthHeader, String serverAuthHeader, int unauthorizedHeader, String user,
                                 String password, String domain, String workstation) {
    this.clientAuthHeader = clientAuthHeader;
    this.serverAuthHeader = serverAuthHeader;
    this.unauthorizedHeader = unauthorizedHeader;
    this.user = user;
    this.password = password;
    this.domain = domain;
    this.workstation = workstation;
  }

  @Override
  public boolean authorizeRequest(String address, HttpServletRequest request, HttpServletResponse response,
                                  boolean addAuthorizeMessageInProxy)
      throws IOException {
    String auth = request.getHeader(clientAuthHeader);
    if (auth == null) {
      response.setStatus(unauthorizedHeader);
      response.addHeader(serverAuthHeader, "NTLM");
      return false;
    }
    if (isNtlmTypeN(auth, 1)) {
      response.setStatus(unauthorizedHeader);
      response.setHeader(serverAuthHeader, TYPE_2_MESSAGE);
      return false;
    } else if (isNtlmTypeN(auth, 3)) {
      response.setStatus(SC_OK);
      if (addAuthorizeMessageInProxy) {
        response.getWriter().print(AUTHORIZED);
      }
      return true;
    } else {
      response.setStatus(SC_UNAUTHORIZED);
      return false;
    }
  }

  private static boolean isNtlmTypeN(String header, int n) {
    if (!header.startsWith("NTLM ")) {
      return false;
    }

    String base64 = header.substring(5);
    byte[] asByteArray = Base64.decode(base64);
    byte type = asByteArray[8];
    return type == n;
  }

  public static class Builder {

    private String clientAuthHeader;
    private String serverAuthHeader;
    private int unauthorizedHeader;
    private String user;
    private String password;
    private String domain;
    private String workstation;

    public Builder setClientAuthHeader(String clientAuthHeader) {
      this.clientAuthHeader = clientAuthHeader;
      return this;
    }

    public Builder setServerAuthHeader(String serverAuthHeader) {
      this.serverAuthHeader = serverAuthHeader;
      return this;
    }

    public Builder setUnauthorizedHeader(int unauthorizedHeader) {
      this.unauthorizedHeader = unauthorizedHeader;
      return this;
    }

    public Builder setUser(String user) {
      this.user = user;
      return this;
    }

    public Builder setPassword(String password) {
      this.password = password;
      return this;
    }

    public Builder setDomain(String domain) {
      this.domain = domain;
      return this;
    }

    public Builder setWorkstation(String workstation) {
      this.workstation = workstation;
      return this;
    }

    public NtlmProxyTestAuthorizer build() throws Exception {
      return new NtlmProxyTestAuthorizer(clientAuthHeader, serverAuthHeader, unauthorizedHeader, user, password, domain,
                                         workstation);
    }
  }
}
