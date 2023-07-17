/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;


import static java.util.Objects.requireNonNull;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.policy.api.OperationPolicyPointcutParametersFactory;
import org.mule.runtime.policy.api.PolicyPointcutParameters;

import java.util.Map;

/**
 * HTTP request operation policy pointcut parameters factory.
 *
 * @since 1.0
 */
public class HttpRequestPolicyPointcutParametersFactory implements OperationPolicyPointcutParametersFactory {

  public static final String PATH_PARAMETER_NAME = "path";
  public static final String METHOD_PARAMETER_NAME = "method";
  private final static ComponentIdentifier requestIdentifier =
      builder().namespace("http").name("request").build();

  @Override
  public boolean supportsOperationIdentifier(ComponentIdentifier operationIdentifier) {
    return requestIdentifier.equals(operationIdentifier);
  }

  /**
   * This method is used in Mule versions prior to 4.1.2, so, as long as HTTP connector supports those versions, this
   * implementation has to remain here
   */
  @Override
  public PolicyPointcutParameters createPolicyPointcutParameters(Component requester,
                                                                 Map<String, Object> operationParameters) {
    requireNonNull(requester, "Cannot create a policy pointcut parameter instance without a valid component");
    String pathParameter = (String) operationParameters.get(PATH_PARAMETER_NAME);
    String methodParameter = (String) operationParameters.get(METHOD_PARAMETER_NAME);
    return new HttpRequestPolicyPointcutParameters(requester, pathParameter, methodParameter);
  }

  @Override
  public PolicyPointcutParameters createPolicyPointcutParameters(Component requester,
                                                                 Map<String, Object> operationParameters,
                                                                 PolicyPointcutParameters sourceParameters) {
    requireNonNull(requester,
                   "Cannot create a policy pointcut parameter instance without a valid component");

    String pathParameter = (String) operationParameters.get(PATH_PARAMETER_NAME);
    String methodParameter = (String) operationParameters.get(METHOD_PARAMETER_NAME);

    return new HttpRequestPolicyPointcutParameters(requester,
                                                   sourceParameters,
                                                   pathParameter,
                                                   methodParameter);
  }
}
