/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors;

import static org.mule.extension.http.api.HttpHeaders.Names.X_FORWARDED_FOR;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.CORS;

import org.mule.modules.cors.attributes.KernelTestAttributesBuilder;
import org.mule.modules.cors.result.KernelTestResult;
import org.mule.modules.cors.tests.functional.ResourceRequestsArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.listener.intercepting.cors.parameters.CorsHttpParameters;
import org.mule.test.http.functional.listener.intercepting.cors.runner.CorsHttpAttributesBuilder;
import org.mule.test.http.functional.listener.intercepting.cors.runner.CorsHttpEndpoint;
import org.mule.test.http.functional.listener.intercepting.cors.runner.CorsRequestExecutor;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Runs all resource requests tests defined in CORS kernel.
 *
 * Other tests have been added to deal with HTTP specifics
 */
@Feature(HTTP_EXTENSION)
@Story(CORS)
public class CorsResourceRequestTestCase
    extends ResourceRequestsArtifactFunctionalTestCase<CorsHttpParameters, CorsHttpEndpoint> {

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

  @Test
  public void appendHeadersOnSimpleRequest() {
    CorsHttpParameters parameters = simpleGet();
    KernelTestResult response = run(parameters, appendHeadersEndpoint());
    check(response)
        .origin(ORIGIN)
        .noExposeHeaders()
        .header("user-agent", "Mule 4.0.0")
        .header("x-miniverse", "Rick and Morty miniverse")
        .payload(PAYLOAD).go();
  }

  @Test
  public void appendHeadersOnComplexRequest() {
    CorsHttpParameters parameters = complexGet();
    KernelTestResult response = run(parameters, appendHeadersEndpoint());
    check(response)
        .origin(ORIGIN)
        .exposeHeaders(X_FORWARDED_FOR.toLowerCase())
        .header("user-agent", "Mule 4.0.0")
        .header("x-miniverse", "Rick and Morty miniverse")
        .payload(PAYLOAD).go();
  }

  @Test
  public void errorInFlowOnSimpleRequest() {
    CorsHttpParameters parameters = simpleGet();
    KernelTestResult response = run(parameters, errorInFlowEndpoint());
    check(response)
        .origin(ORIGIN)
        .noExposeHeaders()
        .payloadDiffers(PAYLOAD).go();
  }

  @Test
  public void errorInFlowOnComplexRequest() {
    CorsHttpParameters parameters = complexGet();
    KernelTestResult response = run(parameters, errorInFlowEndpoint());
    check(response)
        .origin(ORIGIN)
        .exposeHeaders(X_FORWARDED_FOR.toLowerCase())
        .payloadDiffers(PAYLOAD).go();
  }

  @Test
  public void errorInFlowAppendHeadersOnSimpleRequest() {
    CorsHttpParameters parameters = simpleGet();
    KernelTestResult response = run(parameters, errorInFlowAppendHeadersEndpoint());
    check(response)
        .origin(ORIGIN)
        .noExposeHeaders()
        .header("user-agent", "Mule 4.0.0")
        .header("x-miniverse", "Rick and Morty miniverse")
        .payloadDiffers(PAYLOAD).go();
  }

  @Test
  public void errorInFlowAppendHeadersOnComplexRequest() {
    CorsHttpParameters parameters = complexGet();
    KernelTestResult response = run(parameters, errorInFlowAppendHeadersEndpoint());
    check(response)
        .origin(ORIGIN)
        .exposeHeaders(X_FORWARDED_FOR.toLowerCase())
        .header("user-agent", "Mule 4.0.0")
        .header("x-miniverse", "Rick and Morty miniverse")
        .payloadDiffers(PAYLOAD).go();
  }

  @After
  public void tearDown() {
    request.dispose();
  }

  private CorsHttpParameters simpleGet() {
    return attributesBuilder().withOrigin(ORIGIN).withMethod(GET).build();
  }

  private CorsHttpParameters complexGet() {
    return attributesBuilder().withOrigin(ORIGIN)
        .withMethod(GET)
        .withHeader("X-Custom-Header", "custom-value")
        .build();
  }

  @Override
  public CorsHttpEndpoint basic() {
    return new CorsHttpEndpoint("basic", basicPort.getValue());
  }

  @Override
  public CorsHttpEndpoint publicResource() {
    return new CorsHttpEndpoint("public-resource", publicResourcePort.getValue());
  }

  @Override
  public CorsHttpEndpoint allowCredentials() {
    return new CorsHttpEndpoint("allow-credentials", allowCredentialsPort.getValue());
  }

  @Override
  public CorsHttpEndpoint allowCredentialsPublicResource() {
    return new CorsHttpEndpoint("allow-credentials-public-resource", allowCredentialsPublicResourcePort.getValue());
  }

  @Override
  public KernelTestAttributesBuilder<CorsHttpParameters> attributesBuilder() {
    return new CorsHttpAttributesBuilder();
  }

  @Override
  public KernelTestResult run(CorsHttpParameters parameters, CorsHttpEndpoint endpoint) {
    return request.execute(parameters, endpoint);
  }

  @Override
  public void assertCorsHeadersOnSimpleHeadRequest(KernelTestResult response) {
    check(response).origin(ORIGIN).noExposeHeaders().noPayload().go();
  }

  private CorsHttpEndpoint appendHeadersEndpoint() {
    return new CorsHttpEndpoint("listener-appends-headers", basicPort.getValue());
  }

  private CorsHttpEndpoint errorInFlowEndpoint() {
    return new CorsHttpEndpoint("listener-error-no-extra-headers", basicPort.getValue());
  }

  private CorsHttpEndpoint errorInFlowAppendHeadersEndpoint() {
    return new CorsHttpEndpoint("listener-error-with-headers", basicPort.getValue());
  }
}
