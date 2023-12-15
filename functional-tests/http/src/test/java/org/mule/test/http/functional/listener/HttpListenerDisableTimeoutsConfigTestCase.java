/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.runtime.core.api.config.MuleProperties.MULE_DISABLE_RESPONSE_TIMEOUT;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import org.junit.Rule;
import org.junit.Test;

public class HttpListenerDisableTimeoutsConfigTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Rule
  public SystemProperty disableTimeouts = new SystemProperty(MULE_DISABLE_RESPONSE_TIMEOUT, "true");

  @Override
  protected String getConfigFile() {
    return "http-listener-disable-timeouts-config.xml";
  }

  @Test
  public void httpListenerDefaultResponseTimeout() throws Exception {
    final CoreEvent res = flowRunner("httpFlowWithDefaultResponseTimeout")
        .withVariable("port", port.getNumber())
        .withPayload("hi")
        .run();

    assertThat(res.getMessage().getPayload().getValue(), is("hifolks"));
  }

  @Test
  public void httpListenerCustomResponseTimeout() throws Exception {
    final CoreEvent res = flowRunner("httpFlowWithCustomResponseTimeout")
        .withVariable("port", port.getNumber())
        .withPayload("hi")
        .run();

    assertThat(res.getMessage().getPayload().getValue(), is("hifolks"));
  }
}
