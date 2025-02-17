/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.mule.runtime.core.api.util.ClassUtils.getClassPathRoot;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_FOUND;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.STATIC_RESOURCE_LOADER;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static java.lang.String.format;

import static org.apache.http.impl.client.HttpClientBuilder.create;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.runtime.api.lifecycle.CreateException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import io.qameta.allure.Story;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Story(STATIC_RESOURCE_LOADER)
public class HttpListenerStaticResourcesTestCase extends AbstractHttpTestCase {

  public static final String INDEX_HTML_CONTENT = "Test index.html";
  public static final String MAIN_HTML_CONTENT = "Test main.html";
  public static final String TESTING_ROOT_FOLDER_SYSTEM_PROPERTY = "test.root";

  @Rule
  public DynamicPort port1 = new DynamicPort("port1");
  @Rule
  public DynamicPort port2 = new DynamicPort("port2");
  @Rule
  public DynamicPort port3 = new DynamicPort("port3");
  @Rule
  public SystemProperty serverKeyStoreFile = new SystemProperty("serverKeyStoreFile",
                                                                getDefaultEnvironmentConfiguration().getTestServerKeyStore());

  @Rule
  public SystemProperty serverKeyStoreType = new SystemProperty("serverKeyStoreType",
                                                                getDefaultEnvironmentConfiguration().getTestStoreType());
  @Rule
  public SystemProperty testRoot = new SystemProperty(TESTING_ROOT_FOLDER_SYSTEM_PROPERTY,
                                                      getClassPathRoot(HttpListenerStaticResourcesTestCase.class).getPath());
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private int responseCode;
  private String payload;
  private String contentType;
  private TlsContextFactory tlsContextFactory;

  @Override
  protected String getConfigFile() {
    return "http-listener-static-resource-test.xml";
  }

  @Before
  public void setup() throws CreateException {
    // Configure trust store in the client with the certificate of the server.
    tlsContextFactory = TlsContextFactory.builder()
        .trustStorePath(getDefaultEnvironmentConfiguration().getTestGenericTrustKeyStore())
        .trustStoreType(getDefaultEnvironmentConfiguration().getTestStoreType())
        .trustStorePassword("mulepassword")
        .build();
  }

  @Test
  public void httpUrlWithoutExplicitResourceShouldReturnDefaultDocument() throws Exception {
    String url = format("http://localhost:%d/static", port1.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
    assertThat(payload, is(INDEX_HTML_CONTENT));
  }

  @Test
  public void httpUrlRequestingExplicitResourceShouldReturnResource() throws Exception {
    String url = format("http://localhost:%d/static/main.html", port1.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
    assertThat(payload, is(MAIN_HTML_CONTENT));
  }

  @Test
  public void httpUrlRequestingNonexistentResourceShouldReturnNotFoundStatus() throws Exception {
    String url = format("http://localhost:%d/static/foo.html", port1.getNumber());
    executeRequest(url);
    assertThat(responseCode, is(NOT_FOUND.getStatusCode()));
    assertThat(payload, is("Resource '/foo.html' was not found."));
  }

  @Test
  public void notFoundResourcesAreEscaped() throws Exception {
    String url =
        format("http://localhost:%d/static/%s", port1.getNumber(), "%3Cscript%3Ealert%28%27hello%27%29%3B%3C%2Fscript%3E.html");
    executeRequest(url);
    assertThat(responseCode, is(NOT_FOUND.getStatusCode()));
    assertThat(payload, is("Resource '/&lt;script&gt;alert('hello');&lt;/script&gt;.html' was not found."));
  }

  @Test
  public void contentTypeForDefaultResourceShouldBeTextHtml() throws Exception {
    String url = format("http://localhost:%d/static", port1.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
    assertResponseContentType("text/html");
  }

  @Test
  public void contentTypeShouldBeDeterminedFromResource() throws Exception {
    String url = format("http://localhost:%d/static/image.gif", port1.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
    assertResponseContentType("image/gif");
  }

  @Test
  public void httpsUrlWithoutExplicitResourceShouldReturnDefaultDocument() throws Exception {
    String url = format("https://localhost:%d/static", port2.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
    assertThat(payload, is(INDEX_HTML_CONTENT));
  }

  @Test
  public void httpsUrlRequestingExplicitResourceShouldReturnResource() throws Exception {
    String url = format("https://localhost:%d/static/main.html", port2.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
    assertThat(payload, is(MAIN_HTML_CONTENT));
  }

  @Test
  public void httpsUrlRequestingNonexistentResourceShouldReturnNotFoundStatus() throws Exception {
    String url = format("https://localhost:%d/static/foo.html", port2.getNumber());
    executeRequest(url);
    assertThat(NOT_FOUND.getStatusCode(), is(responseCode));
    assertThat(payload, is("Resource '/foo.html' was not found."));
  }

  private void assertResponseContentType(String expectedContentType) {
    assertThat(contentType, startsWith(expectedContentType));
  }

  /**
   * Test that endpoints bound to the same http port but different path work with the static resource MP
   */
  @Test
  public void testFlowBindingOnSamePort() throws Exception {
    String url = format("http://localhost:%d/echo", port1.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));

    url = format("https://localhost:%d/echo", port2.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
  }

  @Test
  public void httpUrlWithRootAddressShouldReturnDefaultDocument() throws Exception {
    String url = format("http://localhost:%d/", port3.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
    assertThat(payload, is(INDEX_HTML_CONTENT));
  }

  @Test
  public void httpUrlExplicitResourceInRootPathShouldReturnResource() throws Exception {
    String url = format("http://localhost:%d/index.html", port3.getNumber());
    executeRequest(url);
    assertThat(OK.getStatusCode(), is(responseCode));
    assertThat(payload, is(INDEX_HTML_CONTENT));
  }

  @Test
  public void nullHttpAttributes() throws Exception {
    expectedException.expectCause(instanceOf(IllegalArgumentException.class));
    expectedException.expectMessage("There are no HTTP attributes defined.");
    flowRunner("null-http").run();
  }

  @Test
  public void onlyServeFilesWithinBasePath() throws Exception {
    String url = format("http://localhost:%d/static/../http-listener-static-resource-test.xml", port1.getNumber());
    executeRequest(url);
    assertThat(NOT_FOUND.getStatusCode(), is(responseCode));
    assertThat(payload, is("Resource '/../http-listener-static-resource-test.xml' was not found."));
  }

  private void executeRequest(String url) throws Exception {
    try (CloseableHttpClient httpClient = buildClient(url.startsWith("https"))) {
      HttpGet httpGet = new HttpGet(url);
      try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
        responseCode = response.getStatusLine().getStatusCode();
        payload = IOUtils.toString(response.getEntity().getContent());
        Header contentTypeHeader = response.getFirstHeader(CONTENT_TYPE);
        if (contentTypeHeader != null) {
          contentType = contentTypeHeader.getValue();
        }
      }
    }
  }

  private CloseableHttpClient buildClient(boolean isHttps) throws Exception {
    HttpClientBuilder builder = create();
    if (isHttps) {
      builder.setSSLContext(tlsContextFactory.createSslContext());
    }
    return builder.build();
  }
}
