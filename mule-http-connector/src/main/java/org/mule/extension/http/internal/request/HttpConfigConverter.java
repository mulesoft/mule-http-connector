/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;

import java.lang.reflect.Field;

/**
 * Converts the policy's {@code HttpRequesterConfig} into a domain-friendly config before
 * calling {@code builder.toHttpRequestBuilder(...)} to avoid loader constraint issues.
 */
final class HttpConfigConverter {

  private HttpConfigConverter() {}

  /**
   * Creates a domain-friendly HttpRequesterConfig from the policy config,
   * then calls {@code builder.toHttpRequestBuilder(...)} with it.
   */
  public static HttpRequestBuilder createRequestBuilder(
                                                        HttpRequesterRequestBuilder builder,
                                                        HttpRequesterConfig policyConfig) {

    try {
      ClassLoader domainClassLoader = builder.getClass().getClassLoader();

      // load domain's HttpRequesterConfig
      Class<?> domainConfigClass = Class.forName(
                                                 "org.mule.extension.http.internal.request.HttpRequesterConfig",
                                                 false,
                                                 domainClassLoader);

      // instantiate a domain config
      Object domainConfigObject = domainConfigClass.getDeclaredConstructor().newInstance();

      // copy five existing fields from policy config -> domain config
      copyField(policyConfig, domainConfigObject, "urlConfiguration");
      copyField(policyConfig, domainConfigObject, "requestSettings");
      copyField(policyConfig, domainConfigObject, "responseSettings");
      copyField(policyConfig, domainConfigObject, "muleContext");
      copyField(policyConfig, domainConfigObject, "cookieManager");

      // call builder.toHttpRequestBuilder(domainConfigObject)
      return (HttpRequestBuilder) builder
          .getClass()
          .getMethod("toHttpRequestBuilder", domainConfigClass)
          .invoke(builder, domainConfigObject);

    } catch (Exception e) {
      throw new RuntimeException(
                                 "Could not convert policy config to domain config", e);
    }
  }

  /**
   * Copies a single private field by name from policy config to the new domain config object.
   */
  private static void copyField(
                                HttpRequesterConfig policyConfig,
                                Object domainConfig,
                                String fieldName)
      throws Exception {

    // access field of the policy config
    Field policyField = policyConfig.getClass().getDeclaredField(fieldName);
    policyField.setAccessible(true);
    Object fieldValue = policyField.get(policyConfig);

    // set that value into the domain config
    Field domainField = domainConfig.getClass().getDeclaredField(fieldName);
    domainField.setAccessible(true);
    domainField.set(domainConfig, fieldValue);
  }
}
