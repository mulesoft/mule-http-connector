/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.http.policy.api.SourcePolicyAwareAttributes.SourceAttribute.HEADERS;
import static org.mule.runtime.http.policy.api.SourcePolicyAwareAttributes.SourceAttribute.REQUEST_PATH;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.policy.PolicyProvider;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import org.mule.runtime.http.policy.api.SourcePolicyAwareAttributes;
import org.mule.runtime.policy.api.PolicyPointcutParameters;
import org.mule.runtime.policy.api.SourcePolicyPointcutParametersFactory;

import javax.inject.Inject;

/**
 * HTTP request operation policy pointcut parameters factory.
 *
 * @since 1.0
 */
public class HttpListenerPolicyPointcutParametersFactory implements SourcePolicyPointcutParametersFactory {

  private PolicyProvider policyProvider;

  private final static ComponentIdentifier listenerIdentifier =
      builder().namespace("http").name("listener").build();

  @Override
  public boolean supportsSourceIdentifier(ComponentIdentifier sourceIdentifier) {
    return listenerIdentifier.equals(sourceIdentifier);
  }

  @Override
  public <T> PolicyPointcutParameters createPolicyPointcutParameters(Component component,
                                                                     TypedValue<T> attributes) {
    HttpRequestAttributes httpRequestAttributes = requireHttpRequestAttributes(attributes);
    return new HttpListenerPolicyPointcutParameters(requireNonNull(component,
                                                                   "Cannot create a policy pointcut parameter instance without a component"),
                                                    policyProvider.sourcePolicyAwareAttributes().requires(REQUEST_PATH)
                                                        ? httpRequestAttributes.getRequestPath()
                                                        : "",
                                                    httpRequestAttributes.getMethod(),
                                                    getHeaders(httpRequestAttributes));
  }

  private MultiMap<String, String> getHeaders(HttpRequestAttributes httpRequestAttributes) {
    MultiMap<String, String> headers = emptyMultiMap();
    if (policyProvider.sourcePolicyAwareAttributes().requires(HEADERS)) {
      headers = new CaseInsensitiveMultiMap(httpRequestAttributes.getHeaders());
      headers.keySet().retainAll(((SourcePolicyAwareAttributes) policyProvider.sourcePolicyAwareAttributes()).getHeaders());
    }
    return headers;
  }

  private static <T> HttpRequestAttributes requireHttpRequestAttributes(TypedValue<T> attributes) {
    if (!(attributes.getValue() instanceof HttpRequestAttributes)) {
      throw new IllegalArgumentException(format("Cannot create a policy pointcut parameter instance from a message which attributes is not an instance of %s, the current attribute instance type is: %s",
                                                HttpRequestAttributes.class.getName(),
                                                attributes.getClass().getName()));
    }

    return (HttpRequestAttributes) attributes.getValue();
  }

  @Inject
  public void setPolicyProvider(PolicyProvider policyProvider) {
    this.policyProvider = policyProvider;
  }
}
