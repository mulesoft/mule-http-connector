/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static java.lang.String.format;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.api.util.Preconditions.checkNotNull;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.policy.api.PolicyPointcutParameters;
import org.mule.runtime.policy.api.SourcePolicyPointcutParametersFactory;

/**
 * HTTP request operation policy pointcut parameters factory.
 * 
 * @since 1.0
 */
public class HttpListenerPolicyPointcutParametersFactory implements SourcePolicyPointcutParametersFactory {

  private final static ComponentIdentifier listenerIdentifier =
      builder().namespace("http").name("listener").build();

  @Override
  public boolean supportsSourceIdentifier(ComponentIdentifier sourceIdentifier) {
    return listenerIdentifier.equals(sourceIdentifier);
  }

  @Override
  public <T> PolicyPointcutParameters createPolicyPointcutParameters(Component component,
                                                                     TypedValue<T> attributes) {
    checkNotNull(component, "Cannot create a policy pointcut parameter instance without a component");
    checkArgument(attributes.getValue() instanceof HttpRequestAttributes,
                  () -> format("Cannot create a policy pointcut parameter instance from a message which attributes is not an instance of %s, the current attribute instance type is: %s",
                               HttpRequestAttributes.class.getName(),
                               attributes != null ? attributes.getClass().getName() : "null"));

    HttpRequestAttributes httpRequestAttributes = (HttpRequestAttributes) attributes.getValue();
    return new HttpListenerPolicyPointcutParameters(component, httpRequestAttributes.getRequestPath(),
                                                    httpRequestAttributes.getMethod());
  }

}
