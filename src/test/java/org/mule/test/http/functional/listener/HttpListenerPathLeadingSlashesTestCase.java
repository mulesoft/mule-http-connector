/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.mule.tck.junit4.FlakyTest;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.util.Arrays;
import java.util.Collection;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.StringEntity;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class HttpListenerPathLeadingSlashesTestCase extends AbstractHttpTestCase {

  @ClassRule
  public static DynamicPort listenPort1 = new DynamicPort("port1");
  @ClassRule
  public static DynamicPort listenPort2 = new DynamicPort("port2");
  @ClassRule
  public static DynamicPort listenPort3 = new DynamicPort("port3");
  @ClassRule
  public static DynamicPort listenPort4 = new DynamicPort("port4");
  @ClassRule
  public static SystemProperty path = new SystemProperty("path", "path");
  @ClassRule
  public static SystemProperty path2 = new SystemProperty("path2", "path2");
  @ClassRule
  public static SystemProperty anotherPath = new SystemProperty("path3", "anotherPath");
  @ClassRule
  public static SystemProperty pathSubPath = new SystemProperty("path4", "path/subpath");

  private final String testPath;
  private final int testPort;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {listenPort1.getNumber(), path.getValue()},
        {listenPort2.getNumber(), "prefix/" + path2.getValue()},
        {listenPort3.getNumber(), "prefix/" + anotherPath.getValue()},
        {listenPort4.getNumber(), "prefix/" + pathSubPath.getValue()},
    });
  }

  public HttpListenerPathLeadingSlashesTestCase(int port, String path) {
    this.testPath = path;
    this.testPort = port;
  }

  @Override
  protected String getConfigFile() {
    return "http-listener-path-leading-slashes-config.xml";
  }

  @Test
  @FlakyTest
  public void callPath() throws Exception {
    final String url = String.format("http://localhost:%d/%s", testPort, testPath);
    final Response response = Request.Post(url).body(new StringEntity(testPath)).connectTimeout(1000).execute();
    assertThat(response.returnContent().asString(), is(testPath));
  }

}
