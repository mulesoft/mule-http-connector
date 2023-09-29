/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static org.mule.extension.http.internal.policy.ReflectiveHttpListenerPolicyPointcutParametersFactory.SOURCE_POLICY_AWARE_ATTRIBUTES_CLASS_NAME;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;
import static org.mule.runtime.core.api.util.ClassUtils.isClassOnPath;

import org.mule.extension.http.internal.policy.CompatibilityHttpListenerPolicyPointcutParametersFactory;
import org.mule.extension.http.internal.policy.HttpListenerOnDomainPolicyPointcutParametersFactory;
import org.mule.extension.http.internal.policy.ReflectiveHttpListenerPolicyPointcutParametersFactory;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.policy.PolicyProvider;
import org.mule.runtime.policy.api.PolicyPointcutParameters;
import org.mule.runtime.policy.api.SourcePolicyPointcutParametersFactory;

import java.util.Optional;

import javax.inject.Inject;

/**
 * HTTP request operation policy pointcut parameters factory.
 *
 * @since 1.0
 */
public class HttpListenerPolicyPointcutParametersFactory implements SourcePolicyPointcutParametersFactory, Initialisable {

  private final static ComponentIdentifier listenerIdentifier =
      builder().namespace("http").name("listener").build();

  private Optional<PolicyProvider> policyProvider;
  private SourcePolicyPointcutParametersFactory factoryDelegate;

  @Override
  public boolean supportsSourceIdentifier(ComponentIdentifier sourceIdentifier) {
    return listenerIdentifier.equals(sourceIdentifier);
  }

  @Override
  public <T> PolicyPointcutParameters createPolicyPointcutParameters(Component component,
                                                                     TypedValue<T> attributes) {
    return factoryDelegate.createPolicyPointcutParameters(component, attributes);
  }

  @Inject
  public void setPolicyProvider(Optional<PolicyProvider> policyProvider) {
    this.policyProvider = policyProvider;
  }

  @Override
  public void initialise() {
    if (isPointcutAttributesApiAvailable()) {
      if (isInDomain()) {
        factoryDelegate = new HttpListenerOnDomainPolicyPointcutParametersFactory();
      } else {
        factoryDelegate = new ReflectiveHttpListenerPolicyPointcutParametersFactory(policyProvider.get());
      }
    } else
      factoryDelegate = new CompatibilityHttpListenerPolicyPointcutParametersFactory();
  }

  private boolean isPointcutAttributesApiAvailable() {
    return isClassOnPath(SOURCE_POLICY_AWARE_ATTRIBUTES_CLASS_NAME, this.getClass());
  }

  /**
   * When configured in a Domain, Policy provider is not present since it belongs to the App
   */
  private boolean isInDomain() {
    return !policyProvider.isPresent();
  }
}
