/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.core.api.construct.Flow;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import javax.inject.Named;

import io.qameta.allure.Feature;

@Feature(HTTP_EXTENSION)
public class HttpRequestLifecycleTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Inject
  @Named("simpleRequest")
  private Flow simpleRequestFlow;

  @Override
  protected String getConfigFile() {
    return "http-request-lifecycle-config.xml";
  }

  @Test
  public void stoppedConfigMakesRequesterFail() throws Exception {
    verifyRequest();
    Lifecycle requestConfig = registry.<Lifecycle>lookupByName("requestConfig").get();
    requestConfig.stop();
    try {
      expectedException.expectCause(isA(ConnectionException.class));
      runFlow("simpleRequest");
    } finally {
      requestConfig.start();
    }
  }

  @Test
  public void stoppedConfigDoesNotAffectAnother() throws Exception {
    verifyRequest();
    Lifecycle requestConfig = registry.<Lifecycle>lookupByName("requestConfig").get();
    requestConfig.stop();
    verifyRequest("otherRequest");
    requestConfig.start();
  }

  @Test
  public void restartConfig() throws Exception {
    verifyRequest();
    Lifecycle requestConfig = registry.<Lifecycle>lookupByName("requestConfig").get();
    requestConfig.stop();
    requestConfig.start();
    verifyRequest();
  }

  @Test
  public void restartFlow() throws Exception {
    verifyRequest();
    simpleRequestFlow.stop();
    simpleRequestFlow.start();
    verifyRequest();
  }

  private void verifyRequest() throws Exception {
    verifyRequest("simpleRequest");
  }

  private void verifyRequest(String flowName) throws Exception {
    assertThat(runFlow(flowName).getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }
}
