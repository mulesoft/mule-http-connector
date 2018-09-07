/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import org.mule.test.http.functional.AbstractHttpExtensionFunctionalTestCase;

import org.junit.Test;

public class HttpRequestDynamicHostTestCase extends AbstractHttpExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "dynamic-host-request-config.xml";
  }

  private void requestHttp(Integer port) throws Exception {
    flowRunner("requestHTTPFlow").withVariable("host", "localhost").withVariable("port", port).run();
  }

  private void requestHttps(Integer port) throws Exception{
    flowRunner("requestHTTPSFlow").withVariable("host", "localhost").withVariable("port", port).run();
  }

  @Test
  public void requestHttpWithValidPort() throws Exception {
    requestHttp(80);
  }

  @Test
  public void requestHttpsWithValidPort() throws Exception {
    requestHttps(443);
  }

  @Test
  public void requestHttpWithNullPortDefaultTo80() throws Exception {
    requestHttp(null);
  }

  @Test
  public void requestHttpsWithNullPortDefaultTo443() throws Exception {
    requestHttps(null);
  }

  @Test
  public void requestHttpWithNegativePortDefaultTo80() throws Exception {
    requestHttp(-78);
  }

  @Test
  public void requestHttpsWithNegativePortDefaultTo443() throws Exception {
    requestHttps(-78);
  }

}
