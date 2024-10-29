/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.proxy;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class KerberosHttpProxyTestCase extends AbstractHttpRequestTestCase {

  private static final String CLIENT_PRINCIPAL = "client@EXAMPLE.COM";
  private static final String SERVER_PRINCIPAL = "HTTP/localhost@EXAMPLE.COM";

  @ClassRule
  public static DynamicPort kdcPort = new DynamicPort("kdcPort");
  @ClassRule
  public static MiniKdcRule miniKdcRule = new MiniKdcRule(kdcPort.getNumber());

  public static final File workingDir = new File("target");
  @ClassRule
  public static SystemProperty keytabPath;

  static {
    try {
      keytabPath = new SystemProperty("keytabPath", new File(workingDir, "keytab").getCanonicalPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeClass
  public static void setUpKeytab() throws Exception {
    File keytabFile = new File(workingDir, "keytab");
    miniKdcRule.createPrincipal(keytabFile, CLIENT_PRINCIPAL, SERVER_PRINCIPAL);
  }

  @Test
  public void frankensTest() throws Exception {
    String response = runFlow("ntlmAuthRequestWithDomain").getMessage().getPayload().getValue().toString();
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    super.handleRequest(baseRequest, request, response);
  }

  @Override
  protected String getConfigFile() {
    return "http-request-ntlm-proxy-config.xml";
  }
}
