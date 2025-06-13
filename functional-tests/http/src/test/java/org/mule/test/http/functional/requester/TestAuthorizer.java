/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TestAuthorizer {

  /**
   * Method to implement the authorization test logic.
   * 
   * @param address                    target address
   * @param request                    HTTP request
   * @param response                   HTTP response
   * @param addAuthorizeMessageInProxy whether the authorization has to be sent from proxy
   * 
   * @return request authorized
   * @throws IOException exception in request
   */
  boolean authorizeRequest(String address, HttpServletRequest request, HttpServletResponse response,
                           boolean addAuthorizeMessageInProxy)
      throws IOException;
}
