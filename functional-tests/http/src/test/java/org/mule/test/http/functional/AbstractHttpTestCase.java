/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_POLICY_PROVIDER;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HTTP_EXTENSION;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.functional.services.NullPolicyProvider;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import org.mule.runtime.http.api.HttpService;
import org.mule.service.http.TestHttpClient;


import java.util.List;

import io.qameta.allure.Feature;
import org.hamcrest.Matcher;
import org.junit.Rule;

@Feature(HTTP_EXTENSION)
public abstract class AbstractHttpTestCase extends MuleArtifactFunctionalTestCase implements HttpTestCaseRunnerConfig {

  protected static final String DEFAULT_PROCESSING_STRATEGY_CLASSNAME =
      "org.mule.runtime.core.internal.processor.strategy.TransactionAwareStreamEmitterProcessingStrategyFactory";
  protected static final String PROACTOR_PROCESSING_STRATEGY_CLASSNAME =
      "org.mule.runtime.core.internal.processor.strategy.TransactionAwareProactorStreamEmitterProcessingStrategyFactory";

  protected static final int DEFAULT_TIMEOUT = 1000;

  // Expected validation error message for JDK 1.8.0_262.
  protected static final String J8_262_SSL_ERROR_RESPONSE = "General SSLEngine problem";
  // Expected validation error message for JDK 1.8.0_275.
  protected static final String J8_275_SSL_ERROR_RESPONSE = "Certificate signature validation failed";
  // Expected validation error message for JDK 11.
  protected static final String J11_SSL_ERROR_RESPONSE = "PKIX path building failed";
  // Expected validation error message for JDK 17.
  protected static final String J17_SSL_ERROR_RESPONSE = "PKIX path validation failed";
  // Expected validation messages for certificates when testing fips.
  public static final String BOUNCY_CASTLE_CERTIFICATE_UNKNOWN_ERROR_MESSAGE = "certificate_unknown";
  // Expected validation error message when using Netty.
  protected static final String NETTY_SSL_ERROR_RESPONSE = "Received fatal alert: handshake_failure";
  protected static final String APPROPRIATE_PROTOCOL_ERROR =
      "No appropriate protocol (protocol is disabled or cipher suites are inappropriate)";
  protected static final String NETTY_APPROPRIATE_PROTOCOL_ERROR = "Received fatal alert: protocol_version";

  @Rule
  public TestHttpClient httpClient = new TestHttpClient.Builder(getService(HttpService.class)).build();

  @Override
  protected void addBuilders(List<ConfigurationBuilder> builders) {
    super.addBuilders(builders);
    builders.add(new AbstractConfigurationBuilder() {

      @Override
      protected void doConfigure(MuleContext muleContext) {
        muleContext.getCustomizationService().registerCustomServiceImpl(OBJECT_POLICY_PROVIDER, new NullPolicyProvider());
      }
    });
  }

  protected void setDefaultProcessingStrategyFactory(String classname) {
    setProperty(ProcessingStrategyFactory.class.getName(), classname);
  }

  protected void clearDefaultProcessingStrategyFactory() {
    clearProperty(ProcessingStrategyFactory.class.getName());
  }

  protected Matcher<String> sslValidationError() {
    return is(anyOf(containsString(J8_262_SSL_ERROR_RESPONSE),
                    containsString(J8_275_SSL_ERROR_RESPONSE),
                    containsString(J11_SSL_ERROR_RESPONSE),
                    containsString(J17_SSL_ERROR_RESPONSE),
                    containsString(NETTY_SSL_ERROR_RESPONSE),
                    containsString(APPROPRIATE_PROTOCOL_ERROR),
                    containsString(NETTY_APPROPRIATE_PROTOCOL_ERROR)));
  }
}
