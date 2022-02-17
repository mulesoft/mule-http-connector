/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.tls;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import org.apache.commons.lang3.JavaVersion;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class HttpTlsContextClient13ToServer13TestCase extends AbstractHttpTlsContextTestCase {

  private static final String client13 = "tls-1.3-client";
  private static final String SUCCESS_RESPONSE = "success";

  @ClassRule
  public static DynamicPort httpsInternalPort1 = new DynamicPort("internal.port.1");

  @ClassRule
  public static DynamicPort httpsInternalPort2 = new DynamicPort("internal.port.2");

  @ClassRule
  public static SystemProperty clientProtocols = new SystemProperty("jdk.tls.client.protocols", "TLSv1.3");

  @ClassRule
  public static SystemProperty muleSecurityModel = new SystemProperty("mule.security.model", "custom");

  @ClassRule
  public static SystemProperty verboseExceptions = new SystemProperty("mule.verbose.exceptions", "true");

  @Override
  protected String getConfigFile() {
    return "http-client-tls-1.3-server-1.3-test.xml";
  }

  @BeforeClass
  public static void assumeJdkIsCompatible() {
    String[] javaVersion = getProperty("java.version").split("_");
    assumeTrue(isJavaVersionAtLeast(JavaVersion.JAVA_1_8)
        && (!javaVersion[0].equals("1.8.0") || parseInt(javaVersion[1]) > 261));
  }

  // In case of certificate expiration regenerate using tls13/certificate-creation-examples.sh
  @Test
  public void testClient13ToServer13() throws Exception {
    assertThat(flowRunner(client13).keepStreamsOpen().run().getMessage(), hasPayload(equalTo(SUCCESS_RESPONSE)));
  }
}
