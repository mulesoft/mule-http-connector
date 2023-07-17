/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_POLICY_PROVIDER;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import org.mule.runtime.http.api.HttpService;
import org.mule.service.http.TestHttpClient;

import java.util.List;

import io.qameta.allure.Feature;
import org.junit.Rule;

@Feature(HTTP_EXTENSION)
public abstract class AbstractHttpTestCase extends MuleArtifactFunctionalTestCase {

  protected static final int DEFAULT_TIMEOUT = 1000;

  // Expected validation error message for JDK 1.8.0_262.
  protected static final String J8_262_SSL_ERROR_RESPONSE = "General SSLEngine problem";
  // Expected validation error message for JDK 1.8.0_275.
  protected static final String J8_275_SSL_ERROR_RESPONSE = "Certificate signature validation failed";
  // Expected validation error message for JDK 11.
  protected static final String J11_SSL_ERROR_RESPONSE = "PKIX path building failed";

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
}
