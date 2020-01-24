/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONNECTION;
import static org.mule.runtime.http.api.HttpHeaders.Names.HOST;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;
import static org.mule.runtime.http.api.HttpHeaders.Values.CLOSE;

import com.google.common.collect.ImmutableMap;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import org.mule.tck.junit4.rule.SystemProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

public class HttpRequestHeadersTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public SystemProperty host = new SystemProperty("host", "localhost");
  @Rule
  public SystemProperty encoding = new SystemProperty("encoding", CHUNKED);

  @Override
  protected String getConfigFile() {
    return "http-request-headers-config.xml";
  }

  @Test
  public void headerDefaultsOnly() throws Exception {
    flowRunner("headerDefaultsOnly").withPayload(TEST_MESSAGE).run();

    assertThat(getFirstReceivedHeader("testDefault"), equalTo("testDefaultValue"));
  }

  @Test
  public void headerDuplicatedDefaults() throws Exception {
    flowRunner("headerDuplicatedDefaults").withPayload(TEST_MESSAGE).run();
    Collection<String> values = headers.get("testDefault");
    assertThat(values, iterableWithSize(2));
    assertThat(values, containsInAnyOrder("testDefaultValue", "otherDefaultValue"));
  }

  @Test
  public void headerAppendDefault() throws Exception {
    flowRunner("headerAppendDefault").withPayload(TEST_MESSAGE).run();

    assertThat(getFirstReceivedHeader("testDefault"), equalTo("testDefaultValue"));
    assertThat(getFirstReceivedHeader("testName1"), equalTo("testValue1"));
  }

  @Test
  public void headerMultiKeyDefault() throws Exception {
    flowRunner("headerMultiKeyDefault").withPayload(TEST_MESSAGE).run();
    Collection<String> values = headers.get("testDefault");
    assertThat(values, iterableWithSize(2));
    assertThat(values, containsInAnyOrder("testDefaultValue", "testValue2"));
  }

  @Test
  public void sendsHeadersFromList() throws Exception {
    flowRunner("headerList").withPayload(TEST_MESSAGE).withVariable("headerName", "testName2")
        .withVariable("headerValue", "testValue2").run();

    assertThat(getFirstReceivedHeader("testName1"), equalTo("testValue1"));
    assertThat(getFirstReceivedHeader("testName2"), equalTo("testValue2"));
  }

  @Test
  public void sendsHeadersFromMap() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("testName1", "testValue1");
    params.put("testName2", "testValue2");
    flowRunner("headerMap").withPayload(TEST_MESSAGE).withVariable("headers", params).run();

    assertThat(getFirstReceivedHeader("testName1"), equalTo("testValue1"));
    assertThat(getFirstReceivedHeader("testName2"), equalTo("testValue2"));
  }

  @Test
  public void overridesHeaders() throws Exception {
    MultiMap<String, String> params = new MultiMap<>();
    params.put("testName1", "testValueNew");
    params.put("testName2", "testValue2");
    flowRunner("headerOverride").withPayload(TEST_MESSAGE).withVariable("headers", params).run();

    final Collection<String> values = headers.get("testName1");
    assertThat(values, containsInAnyOrder("testValue1", "testValueNew"));
    assertThat(getFirstReceivedHeader("testName2"), equalTo("testValue2"));
  }

  @Test
  public void allowsUserAgentOverride() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("User-Agent", "TEST");
    flowRunner("headerMap").withPayload(TEST_MESSAGE).withVariable("headers", params).run();

    assertThat(getFirstReceivedHeader("User-Agent"), equalTo("TEST"));
  }

  @Test
  public void ignoresHttpOutboundPropertiesButAcceptsHeaders() throws Exception {
    final HttpRequestAttributes reqAttributes = mock(HttpRequestAttributes.class);
    when(reqAttributes.getListenerPath()).thenReturn("listenerPath");

    flowRunner("httpHeaders").withPayload(TEST_MESSAGE).withAttributes(reqAttributes).run();

    assertThat(getFirstReceivedHeader("http.scheme"), is("testValue1"));
    assertThat(headers.asMap(), not(hasKey("http.listener.path")));
  }

  @Test
  public void acceptsConnectionHeader() throws Exception {
    flowRunner("connectionHeader").withPayload(TEST_MESSAGE).run();
    assertThat(getFirstReceivedHeader(CONNECTION), is(CLOSE));
  }

  @Test
  public void ignoresConnectionOutboundProperty() throws Exception {
    final HttpRequestAttributes reqAttributes = mock(HttpRequestAttributes.class);
    CaseInsensitiveMultiMap headers = new CaseInsensitiveMultiMap();
    headers.put(CONNECTION, CLOSE);
    when(reqAttributes.getHeaders()).thenReturn(headers);

    flowRunner("outboundProperties").withPayload(TEST_MESSAGE).withAttributes(reqAttributes).run();
    assertThat(getFirstReceivedHeader(CONNECTION), is(not(CLOSE)));
  }

  @Test
  public void acceptsHostHeader() throws Exception {
    flowRunner("hostHeader").withPayload(TEST_MESSAGE).run();
    assertThat(getFirstReceivedHeader(HOST), is(host.getValue()));
  }

  @Test
  public void acceptsTransferEncodingHeader() throws Exception {
    flowRunner("transferEncodingHeader").withPayload(TEST_MESSAGE).run();
    assertThat(getFirstReceivedHeader(TRANSFER_ENCODING), is(encoding.getValue()));
  }

  @Test
  public void headersAddedInTheRegistryAreAddedToTheRequest() throws Exception {
    Optional<HashMap<String, List<String>>> registryHeaders = registry.lookupByName("http.request.fixedHeadersRegistry");
    registryHeaders.map(hh -> hh.put("testName1", asList("testValue1.1", "testValue1.2")));
    registryHeaders.map(hh -> hh.put("testName2", asList("testValue2.1", "testValue2.2", "testValue2.3")));

    flowRunner("headerMap").withPayload(TEST_MESSAGE).run();

    assertThat(headers.get("testName1"), containsInAnyOrder("testValue1.1", "testValue1.2"));
    assertThat(headers.get("testName2"), containsInAnyOrder("testValue2.1", "testValue2.2", "testValue2.3"));
  }
}
