/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.tls;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assume.assumeTrue;

import org.mule.functional.api.exception.ExpectedError;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import org.apache.commons.lang3.JavaVersion;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class HttpTlsContextClient12ToServer13TestCase extends AbstractHttpTlsContextTestCase {

  private static final String client12 = "tls-1.2-client";
  private static final String ERROR_RESPONSE = "failed: Remotely closed.";

  @ClassRule
  public static DynamicPort httpsInternalPort1 = new DynamicPort("internal.port.1");

  @ClassRule
  public static DynamicPort httpsInternalPort2 = new DynamicPort("internal.port.2");

  @ClassRule
  public static SystemProperty clientProtocols = new SystemProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");

  @ClassRule
  public static SystemProperty muleSecurityModel = new SystemProperty("mule.security.model", "custom");

  @ClassRule
  public static SystemProperty verboseExceptions = new SystemProperty("mule.verbose.exceptions", "true");

  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  @Override
  protected String getConfigFile() {
    return "http-client-tls-1.2-server-1.3-test.xml";
  }

  @BeforeClass
  public static void assumeJdkIsCompatible() {
    String[] javaVersion = getProperty("java.version").split("_");
    assumeTrue(isJavaVersionAtLeast(JavaVersion.JAVA_1_8)
        && (!javaVersion[0].equals("1.8.0") || parseInt(javaVersion[1]) > 261));
  }

  @Test
  public void testClient12ToServer13() throws Exception {
    expectedError.expectErrorType("HTTP", "CONNECTIVITY");
    expectedError.expectMessage(anyOf(
        containsString(ERROR_RESPONSE),
        containsString("Received fatal alert: protocol_version")));
    flowRunner(client12).run();
  }
}
