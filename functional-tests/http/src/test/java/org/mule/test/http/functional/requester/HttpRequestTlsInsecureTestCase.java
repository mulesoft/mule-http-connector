/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

import org.mule.functional.api.exception.ExpectedError;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;

/**
 * Sets up two HTTPS clients using a regular trust-store, but one of them insecure. Then two HTTPS servers: one will return a
 * certificate present in the trust-store but with an invalid SAN extension, the other will return a certificate that's not in the
 * trust-store. Verifies that only the insecure client is successful.
 */
@RunnerDelegateTo(Parameterized.class)
@Story(HTTPS)
public class HttpRequestTlsInsecureTestCase extends AbstractHttpTestCase {

  @Parameterized.Parameter
  public String config;

  @Rule
  public DynamicPort port1 = new DynamicPort("port1");
  @Rule
  public SystemProperty insecure = new SystemProperty("insecure", "true");
  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  @Override
  protected String getConfigFile() {
    return config;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {
      {"http-request-insecure-hostname-config.xml"},
      // {"http-request-insecure-certificate-config.xml"}
    });
  }

  @Test
  public void insecureRequest() throws Exception {
    final CoreEvent res = flowRunner("testInsecureRequest").withPayload(TEST_PAYLOAD).run();
    assertThat(res.getMessage().getPayload().getValue(), is(TEST_PAYLOAD));
  }

  // @Test
  public void secureRequest() throws Exception {
    expectedError.expectCause(instanceOf(IOException.class));
    expectedError
        .expectCause(anyOf(hasMessage(containsString(J8_262_SSL_ERROR_RESPONSE)),
                           hasMessage(containsString("No trusted certificate found")),
                           hasMessage(containsString(J11_SSL_ERROR_RESPONSE))));
    flowRunner("testSecureRequest").withPayload(TEST_PAYLOAD).run();
  }

}
