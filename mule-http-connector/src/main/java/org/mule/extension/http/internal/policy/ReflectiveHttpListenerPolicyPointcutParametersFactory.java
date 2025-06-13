/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.policy;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.core.api.util.ClassUtils.getMethod;
import static org.mule.runtime.core.api.util.ClassUtils.loadClass;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.policy.HttpListenerPolicyPointcutParameters;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.policy.PolicyProvider;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import org.mule.runtime.policy.api.PolicyPointcutParameters;
import org.mule.runtime.policy.api.SourcePolicyPointcutParametersFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * HttpListenerPolicyPointcutParametersFactory implementation which makes use of pointcut attributes API which was included in
 * 4.3.0.
 *
 * Since the connector needs to work with older versions of the runtime, the API is invoked using reflection to be able to compile
 * it.
 */
public class ReflectiveHttpListenerPolicyPointcutParametersFactory implements SourcePolicyPointcutParametersFactory {

  public static final String SOURCE_POLICY_AWARE_ATTRIBUTES_CLASS_NAME =
      "org.mule.runtime.http.policy.api.SourcePolicyAwareAttributes";
  private static final String SOURCE_ATTRIBUTE_CLASS_NAME =
      "org.mule.runtime.http.policy.api.SourcePolicyAwareAttributes$SourceAttribute";
  private static final String ATTRIBUTE_CLASS_NAME = "org.mule.runtime.policy.api.PolicyAwareAttributes$Attribute";

  private static final String SOURCE_POLICY_AWARE_ATTRIBUTES_METHOD_NAME = "sourcePolicyAwareAttributes";
  private static final String GET_HEADERS_METHOD_NAME = "getHeaders";
  private static final String REQUIRES_METHOD_NAME = "requires";
  private static final String VALUE_OF_METHOD_NAME = "valueOf";
  private static final String HEADERS_ATTRIBUTE_ENUM_NAME = "HEADERS";
  private static final String REQUEST_PATH_ATTRIBUTE_ENUM_NAME = "REQUEST_PATH";

  private static Method sourcePolicyAwareAttributesMethod;
  private static Method requiresMethod;
  private static Method getHeadersMethod;
  private static Object requestPathEnum;
  private static Object headersEnum;

  private final PolicyProvider policyProvider;

  static {
    try {
      Class<?> sourcePolicyAwareAttributesClass =
          loadClass(SOURCE_POLICY_AWARE_ATTRIBUTES_CLASS_NAME, ReflectiveHttpListenerPolicyPointcutParametersFactory.class);
      Class<?> sourceAttributesClass =
          loadClass(SOURCE_ATTRIBUTE_CLASS_NAME, ReflectiveHttpListenerPolicyPointcutParametersFactory.class);
      Class<?> attributeClass = loadClass(ATTRIBUTE_CLASS_NAME, ReflectiveHttpListenerPolicyPointcutParametersFactory.class);
      sourcePolicyAwareAttributesMethod = PolicyProvider.class.getDeclaredMethod(SOURCE_POLICY_AWARE_ATTRIBUTES_METHOD_NAME);
      getHeadersMethod = sourcePolicyAwareAttributesClass.getDeclaredMethod(GET_HEADERS_METHOD_NAME);
      requiresMethod = sourcePolicyAwareAttributesClass.getDeclaredMethod(REQUIRES_METHOD_NAME, attributeClass);
      Method valueOf = getMethod(sourceAttributesClass, VALUE_OF_METHOD_NAME, null);
      requestPathEnum = invoke(valueOf, null, REQUEST_PATH_ATTRIBUTE_ENUM_NAME);
      headersEnum = invoke(valueOf, null, HEADERS_ATTRIBUTE_ENUM_NAME);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      throw new MuleRuntimeException(createStaticMessage("Exception while trying to load by reflection"), e);
    }
  }

  public ReflectiveHttpListenerPolicyPointcutParametersFactory(PolicyProvider policyProvider) {
    this.policyProvider = policyProvider;
  }

  @Override
  public boolean supportsSourceIdentifier(ComponentIdentifier componentIdentifier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> PolicyPointcutParameters createPolicyPointcutParameters(Component component,
                                                                     TypedValue<T> attributes) {
    requireNonNull(component, "Cannot create a policy pointcut parameter instance without a component");

    HttpRequestAttributes httpRequestAttributes = requireHttpRequestAttributes(attributes);
    Object sourcePolicyAwareAttributes = sourcePolicyAwareAttributes();

    boolean requestPathRequired = requires(requestPathEnum, sourcePolicyAwareAttributes);
    boolean headersRequired = requires(headersEnum, sourcePolicyAwareAttributes);

    if (requestPathRequired || headersRequired) {
      return new HttpListenerPolicyPointcutParameters(component,
                                                      requestPathRequired ? httpRequestAttributes.getRequestPath() : "",
                                                      httpRequestAttributes.getMethod(),
                                                      httpRequestAttributes.getMaskedRequestPath(),
                                                      headersRequired
                                                          ? getHeaders(httpRequestAttributes, sourcePolicyAwareAttributes)
                                                          : emptyMultiMap());
    } else {
      return new HttpListenerPolicyPointcutParameters(component, null, null);
    }
  }

  private MultiMap<String, String> getHeaders(HttpRequestAttributes httpRequestAttributes, Object sourcePolicyAwareAttributes) {
    MultiMap<String, String> headers = new CaseInsensitiveMultiMap(httpRequestAttributes.getHeaders());
    headers.keySet().retainAll(getHeaders(sourcePolicyAwareAttributes));
    return headers;
  }

  private Collection<String> getHeaders(Object sourcePolicyAwareAttributes) {
    return (Collection<String>) invoke(getHeadersMethod, sourcePolicyAwareAttributes);
  }

  private boolean requires(Object attribute, Object sourcePolicyAwareAttributes) {
    return (boolean) invoke(requiresMethod, sourcePolicyAwareAttributes, attribute);
  }

  private Object sourcePolicyAwareAttributes() {
    return invoke(sourcePolicyAwareAttributesMethod, policyProvider);
  }

  private <T> HttpRequestAttributes requireHttpRequestAttributes(TypedValue<T> attributes) {
    if (!(attributes.getValue() instanceof HttpRequestAttributes)) {
      throw new IllegalArgumentException(format("Cannot create a policy pointcut parameter instance from a message which attributes is not an instance of %s, the current attribute instance type is: %s",
                                                HttpRequestAttributes.class.getName(),
                                                attributes.getClass().getName()));
    }

    return (HttpRequestAttributes) attributes.getValue();
  }

  private static Object invoke(Method method, Object instance, Object... args) {
    try {
      return method.invoke(instance, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new MuleRuntimeException(createStaticMessage("Exception while calling method by reflection"), e);
    }
  }
}
