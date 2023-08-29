/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Rule;
import org.junit.Test;

public class HttpAllInterfacesTestCase extends AbstractHttpTestCase {

  private static final String PATH = "flowA";

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  @Override
  protected String getConfigFile() {
    return "http-all-interfaces-config.xml";
  }

  @Test
  public void testAllInterfaces() throws IOException {
    final String url = String.format("http://localhost:%s/%s", listenPort.getNumber(), PATH);
    final Response response = Request.Get(url).connectTimeout(1000).execute();
    assertThat(response.returnContent().asString(), is(PATH));
  }

}
