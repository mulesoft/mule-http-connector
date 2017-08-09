/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.HTTPS;
import org.mule.runtime.core.api.InternalEvent;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import org.junit.Rule;
import org.junit.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(HTTP_EXTENSION)
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
    final InternalEvent res = flowRunner("testFlowGlobalContextClient")
        .withVariable("port", port1.getNumber())
        .withPayload("data")
        .run();
    assertThat(res.getMessage().getPayload().getValue(), is("ok"));
  }

  @Test
  public void customTlsNestedContext() throws Exception {
    final InternalEvent res = flowRunner("testFlowNestedContextClient")
        .withVariable("port", port2.getNumber())
        .withPayload("data")
        .run();
    assertThat(res.getMessage().getPayload().getValue(), is("all right"));
  }

}
