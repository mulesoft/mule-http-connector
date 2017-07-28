/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors;

import org.mule.modules.cors.PreflightKernelTestCase;
import org.mule.modules.cors.result.CorsTestResult;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.listener.intercepting.cors.parameters.CorsParameters;
import org.mule.test.http.functional.listener.intercepting.cors.runner.CorsHttpAttributesBuilder;
import org.mule.test.http.functional.listener.intercepting.cors.runner.CorsHttpEndpoint;
import org.mule.test.http.functional.listener.intercepting.cors.runner.CorsRequestExecutor;

import org.junit.Before;
import org.junit.Rule;

public class CorsPreflightTestCase extends
    PreflightKernelTestCase<CorsParameters, CorsHttpAttributesBuilder, CorsHttpEndpoint> {

  private CorsRequestExecutor request;

  @Rule
  public DynamicPort basicPort = new DynamicPort("basicConfigPort");

  @Rule
  public DynamicPort allowCredentialsPort = new DynamicPort("allowCredentialsConfigPort");

  @Rule
  public DynamicPort publicResourcePort = new DynamicPort("publicResourceConfigPort");

  @Rule
  public DynamicPort allowCredentialsPublicResourcePort = new DynamicPort("allowCredentialsPublicResourceConfigPort");

  @Rule
  public SystemProperty payloadProperty = new SystemProperty("payload", PAYLOAD);

  @Before
  public void setUp() {
    request = new CorsRequestExecutor();
  }

  @Override
  protected String getConfigFile() {
    return "http-listener-cors-interceptor.xml";
  }

  @Override
  protected CorsHttpAttributesBuilder preflight() {
    return new CorsHttpAttributesBuilder().preflight();
  }

  @Override
  protected CorsHttpEndpoint basic() {
    return new CorsHttpEndpoint("basic", basicPort);
  }

  @Override
  protected CorsHttpEndpoint publicResource() {
    return new CorsHttpEndpoint("public-resource", publicResourcePort);
  }

  @Override
  protected CorsHttpEndpoint allowCredentials() {
    return new CorsHttpEndpoint("allow-credentials", allowCredentialsPort);
  }

  @Override
  protected CorsHttpEndpoint allowCredentialsPublicResource() {
    return new CorsHttpEndpoint("allow-credentials-public-resource", allowCredentialsPublicResourcePort);
  }

  @Override
  protected CorsTestResult run(CorsParameters parameters, CorsHttpEndpoint endpoint) {
    return request.execute(parameters, endpoint);
  }
}
