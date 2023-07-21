/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.METHOD_NOT_ALLOWED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_FOUND;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;
import org.mule.test.http.functional.matcher.HttpResponseReasonPhraseMatcher;
import org.mule.test.http.functional.matcher.HttpResponseStatusCodeMatcher;
import org.mule.test.http.utils.SocketRequester;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Rule;
import org.junit.Test;

public class HttpListenerConfigFunctionalTestCase extends AbstractHttpTestCase {

  private static final String NO_LISTENER_ENTITY_FORMAT = "No listener for endpoint: %s";

  private static final Pattern IPADDRESS_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
      + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
  private static final int TIMEOUT = 1000;

  @Rule
  public DynamicPort fullConfigPort = new DynamicPort("fullConfigPort");
  @Rule
  public DynamicPort emptyConfigPort = new DynamicPort("emptyConfigPort");
  @Rule
  public DynamicPort noListenerConfigPort = new DynamicPort("noListenerConfigPort");
  @Rule
  public DynamicPort slashConfigPort = new DynamicPort("slashConfigPort");
  @Rule
  public SystemProperty path = new SystemProperty("path", "path");
  @Rule
  public SystemProperty basePath = new SystemProperty("basePath", "basePath");
  @Rule
  public SystemProperty nonLocalhostIp = new SystemProperty("nonLocalhostIp", getNonLocalhostIp());

  @Override
  protected String getConfigFile() {
    return "http-listener-config-functional-config.xml";
  }

  @Test
  public void emptyConfig() throws Exception {
    final String url = format("http://localhost:%s/%s", emptyConfigPort.getNumber(), path.getValue());
    callAndAssertStatus(url, SC_OK);
  }

  @Test
  public void fullConfig() throws Exception {
    final String url =
        format("http://localhost:%s/%s/%s", fullConfigPort.getNumber(), basePath.getValue(), path.getValue());
    callAndAssertStatus(url, SC_OK);
  }

  @Test
  public void fullConfigWrongPath() throws Exception {
    final String url =
        format("http://localhost:%s/%s/%s", fullConfigPort.getNumber(), basePath.getValue(), path.getValue() + "2");
    callAndAssertStatus(url, SC_NOT_FOUND);
  }

  @Test
  public void listenerConfigOverridesListenerConfig() throws Exception {
    final String url = format("http://%s:%s/%s/%s", nonLocalhostIp.getValue(), fullConfigPort.getNumber(),
                              basePath.getValue(), path.getValue());
    callAndAssertStatus(url, SC_OK);
  }

  @Test
  public void noListenerConfig() throws Exception {
    final String url = format("http://localhost:%s/noListener", noListenerConfigPort.getNumber());
    final HttpResponse httpResponse = callAndAssertStatus(url, NOT_FOUND);
    assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), is(format(NO_LISTENER_ENTITY_FORMAT, "/noListener")));
  }

  @Test
  public void noListenerConfigWithSpecialCharacters() throws Exception {
    String invalidPathWithSpecialCharacters = "/<script></script>";
    SocketRequester socketRequester = new SocketRequester("localhost", noListenerConfigPort.getNumber());
    socketRequester.initialize();
    socketRequester.doRequest("GET " + invalidPathWithSpecialCharacters + " HTTP/1.1");
    assertThat(socketRequester.getResponse(),
               containsString(format(NO_LISTENER_ENTITY_FORMAT, escapeHtml4(invalidPathWithSpecialCharacters))));
    socketRequester.finalizeGracefully();
  }

  @Test
  public void fullConfigWrongMethod() throws Exception {
    final String url = format("http://localhost:%s/%s/%s", fullConfigPort.getNumber(), basePath.getValue(), "post");
    final HttpResponse httpResponse = callAndAssertStatus(url, METHOD_NOT_ALLOWED);
    assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), containsString("Method not allowed for endpoint"));
  }

  @Test
  public void useSlashInPathAndBasePath() throws Exception {
    String baseUrl = format("http://localhost:%s/", slashConfigPort.getNumber());
    assertThat(callAndAssertStatusWithMuleClient(baseUrl + "/", SC_OK), is("1"));
    assertThat(callAndAssertStatusWithMuleClient(baseUrl + "//", SC_OK), is("2"));
    assertThat(callAndAssertStatusWithMuleClient(baseUrl + "///", SC_OK), is("3"));

    callAndAssertStatusWithMuleClient(baseUrl + "////", SC_NOT_FOUND);
  }

  private String callAndAssertStatusWithMuleClient(String url, int expectedStatus) throws Exception {
    HttpRequest request =
        HttpRequest.builder().uri(url).method(GET).entity(new ByteArrayHttpEntity(TEST_PAYLOAD.getBytes())).build();
    final org.mule.runtime.http.api.domain.message.response.HttpResponse response =
        httpClient.send(request, RECEIVE_TIMEOUT, false, null);
    assertThat(response.getStatusCode(), is(expectedStatus));

    return IOUtils.toString(response.getEntity().getContent());
  }

  private HttpResponse callAndAssertStatus(String url, int expectedStatus) throws IOException {
    final Response response = Request.Get(url).connectTimeout(TIMEOUT).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse, HttpResponseStatusCodeMatcher.hasStatusCode(expectedStatus));
    return httpResponse;
  }

  private HttpResponse callAndAssertStatus(String url, HttpStatus expectedStatus) throws IOException {
    HttpResponse httpResponse = callAndAssertStatus(url, expectedStatus.getStatusCode());
    assertThat(httpResponse, HttpResponseReasonPhraseMatcher.hasReasonPhrase(expectedStatus.getReasonPhrase()));
    return httpResponse;
  }

  private String getNonLocalhostIp() {
    try {
      List<InetAddress> candidates = new ArrayList<>();
      Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
      for (NetworkInterface networkInterface : Collections.list(nets)) {
        final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          InetAddress inetAddress = inetAddresses.nextElement();
          if (!inetAddress.isLoopbackAddress() && IPADDRESS_PATTERN.matcher(inetAddress.getHostAddress()).find()) {
            candidates.add(inetAddress);
          }
        }
      }
      if (candidates.isEmpty()) {
        throw new RuntimeException("Could not find network interface different from localhost");
      } else {
        return preferSiteLocal(candidates);
      }
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
  }

  private static String preferSiteLocal(List<? extends InetAddress> candidates) {
    for (InetAddress address : candidates) {
      if (address.isSiteLocalAddress()) {
        // If there is some site local, return that.
        return address.getHostAddress();
      }
    }
    // Otherwise, fallback to the first.
    return candidates.get(0).getHostAddress();
  }

}
