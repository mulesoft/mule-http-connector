/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.http.policy.api.HttpPolicyPointcutParameters;
import org.mule.runtime.policy.api.PolicyPointcutParameters;

/**
 * Specific implementation of {@link PolicyPointcutParameters} for http:request operation.
 * 
 * @since 1.0
 */
public class HttpRequestPolicyPointcutParameters extends HttpPolicyPointcutParameters {

  /**
   * Creates a new {@link PolicyPointcutParameters}
   *
   * @param requester the requester where the policy is being applied.
   * @param path      the target path of the http:request operation.
   * @param method    the HTTP method of the http:request operation.
   */
  public HttpRequestPolicyPointcutParameters(Component requester, String path, String method) {
    super(requester, path, method);
  }

  /**
   * Creates a new {@link PolicyPointcutParameters}
   *
   * @param requester                the requester where the policy is being applied.
   * @param sourcePointcutParameters parameters used to match pointcuts of source policies
   * @param path                     the target path of the http:request operation.
   * @param method                   the HTTP method of the http:request operation.
   */
  public HttpRequestPolicyPointcutParameters(Component requester, PolicyPointcutParameters sourcePointcutParameters, String path,
                                             String method) {
    super(requester, sourcePointcutParameters, path, method);
  }

}
