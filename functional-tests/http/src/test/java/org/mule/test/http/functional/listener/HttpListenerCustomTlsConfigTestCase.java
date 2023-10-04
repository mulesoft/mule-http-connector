/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;

@Story(HTTPS)
public class HttpListenerCustomTlsConfigTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port1 = new DynamicPort("port1");

  @Rule
  public DynamicPort port2 = new DynamicPort("port2");

  @Rule
  public DynamicPort port3 = new DynamicPort("port3");

  @Override
  protected String getConfigFile() {
    return "http-listener-custom-tls-config.xml";
  }

  @Test
  public void customTlsGlobalContext() throws Exception {
    final CoreEvent res = flowRunner("testFlowGlobalContextClient")
        .withVariable("port", port1.getNumber())
        .withPayload("data")
        .keepStreamsOpen()
        .run();
    assertThat(res.getMessage(), hasPayload(equalTo("ok X.509")));
  }

  @Test
  public void customTlsNestedContext() throws Exception {
    final CoreEvent res = flowRunner("testFlowNestedContextClient")
        .withVariable("port", port2.getNumber())
        .withPayload("data")
        .keepStreamsOpen()
        .run();
    assertThat(res.getMessage(), hasPayload(equalTo("all right X.509")));
  }

}
