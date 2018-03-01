/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.proxy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.functional.api.component.FunctionalTestProcessor.getFromFlow;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.http.TestProxyServer;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers;
import org.mule.test.runner.RunnerDelegateTo;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;


@RunnerDelegateTo(Parameterized.class)
public class HttpRequestProxyTlsTestCase extends AbstractHttpTestCase {

  private static final String OK_RESPONSE = "OK";
  private static final String PATH = "/test?key=value";

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");

  @Rule
  public DynamicPort httpPort = new DynamicPort("httpPort");

  @Rule
  public SystemProperty keyStorePathProperty;

  @Rule
  public SystemProperty trustStorePathProperty;

  private TestProxyServer proxyServer = new TestProxyServer(proxyPort.getNumber(), httpPort.getNumber(), true);

  private String requestURI;
  private String requestPayload;
  private String requestHost;

  public HttpRequestProxyTlsTestCase(String keyStorePath, String trustStorePath, String requestHost) {
    this.keyStorePathProperty = new SystemProperty("keyStorePath", keyStorePath);
    this.trustStorePathProperty = new SystemProperty("trustStorePath", trustStorePath);
    this.requestHost = requestHost;
  }

  /**
   * The test will run with two key store / trust store pairs. One has the subject alternative name set to localhost (the default
   * for all TLS tests), and the other one has the name set to "test". We need this to validate that the hostname verification is
   * performed using the host of the request, and not the one of the proxy.
   */
  @Parameterized.Parameters
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {
        {"tls/ssltest-keystore-with-test-hostname.jks", "tls/ssltest-truststore-with-test-hostname.jks", "test"},
        {"tls/ssltest-keystore.jks", "tls/ssltest-cacerts.jks", "localhost"}});
  }

  @Override
  protected String getConfigFile() {
    return "http-request-proxy-tls-config.xml";
  }

  @Test
  public void requestIsSentCorrectlyThroughHttpsProxy() throws Exception {
    getFromFlow(locator, "serverFlow").setEventCallback((context, component, muleContext) -> {
      requestPayload = getPayloadAsString(context.getMessage());
      requestURI = ((HttpRequestAttributes) context.getMessage().getAttributes().getValue()).getRequestUri();
    });

    proxyServer.start();

    CoreEvent event = flowRunner("clientFlow").withPayload(TEST_MESSAGE).withVariable("host", requestHost)
        .withVariable("path", PATH).run();

    assertThat(requestPayload, equalTo(TEST_MESSAGE));
    assertThat(requestURI, equalTo(PATH));
    assertThat((HttpResponseAttributes) event.getMessage().getAttributes().getValue(), HttpMessageAttributesMatchers
        .hasStatusCode(OK.getStatusCode()));
    assertThat(event.getMessage().getPayload().getValue(), equalTo(OK_RESPONSE));
    assertThat(proxyServer.hasConnections(), is(true));

    proxyServer.stop();
  }
}
