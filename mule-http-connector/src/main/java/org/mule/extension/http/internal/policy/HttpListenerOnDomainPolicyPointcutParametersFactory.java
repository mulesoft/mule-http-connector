/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.policy;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.policy.HttpListenerPolicyPointcutParameters;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.policy.api.PolicyPointcutParameters;
import org.mule.runtime.policy.api.SourcePolicyPointcutParametersFactory;

/**
 * When HTTP is on a domain, optimizations based on requested attributes by deployed policies in an Application can't be
 * performed. Therefore, it is assumed that HTTP pathes are always needed and HTTP headers are never needed when creating
 * {@link PolicyPointcutParameters}
 */
public class HttpListenerOnDomainPolicyPointcutParametersFactory implements SourcePolicyPointcutParametersFactory {

  @Override
  public <T> PolicyPointcutParameters createPolicyPointcutParameters(Component component, TypedValue<T> attributes) {
    requireNonNull(component, "Cannot create a policy pointcut parameter instance without a component");
    if (!(attributes.getValue() instanceof HttpRequestAttributes)) {
      throw new IllegalArgumentException(format(
                                                "Cannot create a policy pointcut parameter instance from a message which attributes is not an instance of %s, the current attribute instance type is: %s",
                                                HttpRequestAttributes.class.getName(),
                                                attributes.getClass().getName()));
    }

    HttpRequestAttributes httpRequestAttributes = (HttpRequestAttributes) attributes.getValue();
    return new HttpListenerPolicyPointcutParameters(component,
                                                    httpRequestAttributes.getRequestPath(),
                                                    httpRequestAttributes.getMethod(),
                                                    httpRequestAttributes.getMaskedRequestPath(),
                                                    emptyMultiMap());
  }

  @Override
  public boolean supportsSourceIdentifier(ComponentIdentifier componentIdentifier) {
    throw new UnsupportedOperationException();
  }
}
