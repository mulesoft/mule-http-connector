/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.internal.matchers.Matches;

public class HttpListenerRemoteAddressFormatTestCase extends AbstractHttpTestCase {

  private static final String NO_HOSTNAME_REGEX = "([^/]*):(.*)";
  private static final String WITH_HOSTNAME_REGEX = "([^/]*)/(.*):(.*)";

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Rule
  public DynamicPort port2 = new DynamicPort("port2");

  @Override
  protected String getConfigFile() {
    return "http-listener-remote-address-format-config.xml";
  }

  @Test
  public void addressOnly() throws Exception {
    Response response = Request.Get(url(port)).execute();
    assertThat(IOUtils.toString(response.returnResponse().getEntity().getContent()),
               is(new Matches(NO_HOSTNAME_REGEX)));
  }

  @Test
  public void hostnameAndAddress() throws Exception {

    Response response = Request.Get(url(port2)).execute();
    assertThat(IOUtils.toString(response.returnResponse().getEntity().getContent()),
               is(new Matches(WITH_HOSTNAME_REGEX)));
  }

  private String url(DynamicPort port) {
    return String.format("http://localhost:%s/test", port.getNumber());
  }
}
