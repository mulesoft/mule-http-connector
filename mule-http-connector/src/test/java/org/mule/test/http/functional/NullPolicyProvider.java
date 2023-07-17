/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static java.util.Collections.emptyList;

import org.mule.runtime.core.api.policy.Policy;
import org.mule.runtime.core.api.policy.PolicyProvider;
import org.mule.runtime.policy.api.PolicyPointcutParameters;

import java.util.List;

public class NullPolicyProvider implements PolicyProvider {

  @Override
  public List<Policy> findSourceParameterizedPolicies(PolicyPointcutParameters policyPointcutParameters) {
    return emptyList();
  }

  @Override
  public List<Policy> findOperationParameterizedPolicies(PolicyPointcutParameters policyPointcutParameters) {
    return emptyList();
  }
}
