/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.policy;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;
import static org.mule.runtime.api.metadata.DataType.OBJECT;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.POLICY_SUPPORT;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.policy.HttpListenerPolicyPointcutParameters;
import org.mule.extension.http.api.policy.HttpListenerPolicyPointcutParametersFactory;
import org.mule.extension.http.internal.policy.HttpListenerOnDomainPolicyPointcutParametersFactory;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.policy.api.SourcePolicyPointcutParametersFactory;
import org.mule.tck.junit4.AbstractMuleTestCase;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
@Story(POLICY_SUPPORT)
@Issue("MULE-18403")
public class HttpListenerOnDomainPolicyPointcutParametersFactoryTestCase extends AbstractMuleTestCase {

  private static final String TEST_REQUEST_PATH = "test-request-path";
  private static final String TEST_METHOD = "PUT";

  private final SourcePolicyPointcutParametersFactory factory = new HttpListenerOnDomainPolicyPointcutParametersFactory();
  private final Component component = mock(Component.class);
  private final HttpRequestAttributes httpAttributes = mock(HttpRequestAttributes.class);
  private final TypedValue attributes = new TypedValue<>(mock(Object.class), OBJECT);

  @Test(expected = IllegalArgumentException.class)
  public void failIfAttributesIsNotHttpRequestAttributes() {
    factory.createPolicyPointcutParameters(component, attributes);
  }

  @Test(expected = NullPointerException.class)
  public void failIfComponentLocationIsNull() {
    factory.createPolicyPointcutParameters(null, attributes);
  }

  @Test
  public void policyPointcutParameters() {
    when(httpAttributes.getRequestPath()).thenReturn(TEST_REQUEST_PATH);
    when(httpAttributes.getMethod()).thenReturn(TEST_METHOD);

    HttpListenerPolicyPointcutParameters policyPointcutParameters =
        (HttpListenerPolicyPointcutParameters) factory.createPolicyPointcutParameters(component,
                                                                                      new TypedValue<>(httpAttributes, OBJECT));

    assertThat(policyPointcutParameters.getComponent(), is(component));
    assertThat(policyPointcutParameters.getPath(), is(TEST_REQUEST_PATH));
    assertThat(policyPointcutParameters.getMethod(), is(TEST_METHOD));
  }

}
