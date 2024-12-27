/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.policy.api.HttpPolicyPointcutParameters;
import org.mule.runtime.policy.api.PolicyPointcutParameters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;

/**
 * Specific implementation of {@link PolicyPointcutParameters} for http:listener operation.
 * 
 * @since 1.0
 */
public class HttpListenerPolicyPointcutParameters extends HttpPolicyPointcutParameters {

  private static final Logger LOGGER = getLogger(HttpListenerPolicyPointcutParameters.class);

  /**
   * As long as the connector is compatible with runtime versions < 4.3.0, headers and masked request path need to be set using
   * reflection since they were added in the API in 4.3.0
   */
  private static Method setHeadersMethod;
  private static Method setMaskedRequestPathMethod;

  static {
    try {
      setHeadersMethod = HttpPolicyPointcutParameters.class.getDeclaredMethod("setHeaders", MultiMap.class);
      setMaskedRequestPathMethod = HttpPolicyPointcutParameters.class.getDeclaredMethod("setMaskedRequestPath", String.class);
    } catch (Exception e) {
      LOGGER.debug("setHeaders and setMaskedRequestPath methods are not present in HttpPolicyPointcutParameters class");
    }
  }

  /**
   * Creates a new {@link PolicyPointcutParameters}
   *
   * @param component the component where the policy is being applied.
   * @param path the target path of the incoming request
   * @param method the HTTP method of the incoming request
   */
  public HttpListenerPolicyPointcutParameters(Component component, String path, String method) {
    super(component, path, method);
  }

  /**
   * Creates a new {@link PolicyPointcutParameters}
   *
   * @param component the component where the policy is being applied.
   * @param path the target path of the incoming request
   * @param method the HTTP method of the incoming request
   * @param maskedRequestPath the target path without the base path where the listener is deployed
   * @param headers the HTTP headers of the incoming request
   */
  public HttpListenerPolicyPointcutParameters(Component component, String path, String method, String maskedRequestPath,
                                              MultiMap<String, String> headers) {
    super(component, path, method);
    try {
      if (setHeadersMethod != null && setMaskedRequestPathMethod != null) {
        setHeadersMethod.invoke(this, headers);
        setMaskedRequestPathMethod.invoke(this, maskedRequestPath);
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new MuleRuntimeException(createStaticMessage("Exception while calling setters by reflection"), e);
    }
  }


}
