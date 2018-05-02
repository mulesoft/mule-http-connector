/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.policy;

import static java.util.Collections.emptyMap;
import static java.util.Optional.of;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mule.extension.http.api.policy.HttpRequestPolicyPointcutParametersFactory.PATH_PARAMETER_NAME;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;
import static org.mule.tck.junit4.matcher.IsEmptyOptional.empty;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.POLICY_SUPPORT;

import org.mule.extension.http.api.policy.HttpRequestPolicyPointcutParameters;
import org.mule.extension.http.api.policy.HttpRequestPolicyPointcutParametersFactory;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.policy.api.PolicyPointcutParameters;
import org.mule.tck.junit4.AbstractMuleTestCase;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
@Story(POLICY_SUPPORT)
public class HttpRequestPolicyPointcutParametersFactoryTestCase extends AbstractMuleTestCase {

  private static final ComponentIdentifier HTTP_REQUEST_COMPONENT_IDENTIFIER =
      builder().namespace("http").name("request").build();
  private static final String TEST_REQUEST_PATH = "test-request-path";
  private static final String TEST_METHOD = "PUT";

  private final HttpRequestPolicyPointcutParametersFactory factory = new HttpRequestPolicyPointcutParametersFactory();

  @Test
  public void supportsHttpRequest() {
    assertThat(factory
        .supportsOperationIdentifier(HTTP_REQUEST_COMPONENT_IDENTIFIER),
               is(true));
  }

  @Test
  public void doesNotSupportHttpListener() {
    assertThat(factory
        .supportsOperationIdentifier(builder().namespace("http").name("listener").build()),
               is(false));
  }

  @Test(expected = NullPointerException.class)
  public void failIfComponentLocationIsNull() {
    factory.createPolicyPointcutParameters(null, emptyMap());
  }

  @Test
  public void policyPointcutParameters() {
    Component component = mock(Component.class);
    Map<String, Object> parametersMap =
        ImmutableMap.<String, Object>builder().put(HttpRequestPolicyPointcutParametersFactory.METHOD_PARAMETER_NAME, TEST_METHOD)
            .put(PATH_PARAMETER_NAME, TEST_REQUEST_PATH).build();

    HttpRequestPolicyPointcutParameters policyPointcutParameters =
        (HttpRequestPolicyPointcutParameters) factory.createPolicyPointcutParameters(component, parametersMap);

    assertThat(policyPointcutParameters.getComponent(), is(component));
    assertThat(policyPointcutParameters.getSourceParameters(), empty());
    assertThat(policyPointcutParameters.getPath(), is(TEST_REQUEST_PATH));
    assertThat(policyPointcutParameters.getMethod(), is(TEST_METHOD));
  }

  @Test
  public void policyPointcutParametersWithSourceParameters() {
    Component component = mock(Component.class);
    Map<String, Object> parametersMap =
        ImmutableMap.<String, Object>builder().put(HttpRequestPolicyPointcutParametersFactory.METHOD_PARAMETER_NAME, TEST_METHOD)
            .put(PATH_PARAMETER_NAME, TEST_REQUEST_PATH).build();
    PolicyPointcutParameters sourceParameters = mock(PolicyPointcutParameters.class);

    HttpRequestPolicyPointcutParameters policyPointcutParameters =
        (HttpRequestPolicyPointcutParameters) factory.createPolicyPointcutParameters(component, parametersMap, sourceParameters);

    assertThat(policyPointcutParameters.getComponent(), is(component));
    assertThat(policyPointcutParameters.getSourceParameters(), is(of(sourceParameters)));
    assertThat(policyPointcutParameters.getPath(), is(TEST_REQUEST_PATH));
    assertThat(policyPointcutParameters.getMethod(), is(TEST_METHOD));
  }

}
