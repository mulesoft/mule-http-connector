/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.ntlm;

import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;
import org.mule.test.http.functional.requester.TestAuthorizer;

import java.io.IOException;

import org.eclipse.jetty.server.Request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class AbstractNtlmTestCase extends AbstractHttpRequestTestCase {

  protected static final String USER = "Zaphod";
  protected static final String PASSWORD = "Beeblebrox";
  protected static final String DOMAIN = "Ursa-Minor";
  protected static final String AUTHORIZED = "Authorized";

  protected String requestUrl;

  private TestAuthorizer authorizer;

  public void setupTestAuthorizer(String clientAuthHeader, String serverAuthHeader, int unauthorizedHeader) {
    authorizer = createTestAuthorizer(clientAuthHeader, serverAuthHeader, unauthorizedHeader);
  }

  protected String getDomain() {
    return DOMAIN;
  }

  protected String getWorkstation() {
    return null;
  }

  protected void handleRequest(String address, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (authorizer.authorizeRequest(address, request, response, true)) {
      requestUrl = address;
    }
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    handleRequest(baseRequest.getRequestURL().toString(), request, response);
  }

  private TestAuthorizer createTestAuthorizer(String clientAuthHeader, String serverAuthHeader, int unauthorizedHeader) {
    try {
      TestAuthorizer testAuthorizer = new NtlmProxyTestAuthorizer.Builder().setClientAuthHeader(clientAuthHeader)
          .setServerAuthHeader(serverAuthHeader)
          .setUnauthorizedHeader(unauthorizedHeader)
          .setUser(USER)
          .setPassword(PASSWORD)
          .setDomain(getDomain())
          .setWorkstation(getWorkstation())
          .build();
      return testAuthorizer;

    } catch (Exception e) {
      throw new RuntimeException("Error creating testAuthorizer");
    }
  }

  public TestAuthorizer getAuthorizer() {
    return authorizer;
  }

}
