package org.mule.test.http.functional.proxy;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.runtime.core.api.event.BaseEvent;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

// TODO - MULE-9563 - improve this suite starting a proxy-server and checking that the request went through
public class HttpProxyParamsTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Override
  protected String getConfigFile() {
    return "http-request-proxy-params-config.xml";
  }

  @Test
  public void proxyWithNonProxyHostsParam() throws Exception {
    final BaseEvent event = runFlow("nonProxyParamProxy");
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(OK.getStatusCode()));
    assertThat(event.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }

  @Test
  public void innerProxyWithNonProxyHostsParam() throws Exception {
    final BaseEvent event = runFlow("innerNonProxyParamProxy");
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(OK.getStatusCode()));
    assertThat(event.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }

  @Test
  public void proxyWithMultipleHostsNonProxyHostsParam() throws Exception {
    final BaseEvent event = runFlow("innerNonProxyParamProxyMultipleHosts");
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(OK.getStatusCode()));
    assertThat(event.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }

  @Test
  public void proxyWithoutNonProxyHostsParam() throws Exception {
    expectedException.expectCause(isA(HttpRequestFailedException.class));
    expectedException.expectMessage(containsString("Connection refused."));
    runFlow("refAnonymousProxy");
    fail("Request should fail as there is no proxy configured");
  }

  @Test
  public void proxyWithAnotherHostNonProxyHostsParam() throws Exception {
    expectedException.expectCause(isA(HttpRequestFailedException.class));
    expectedException.expectMessage(containsString("Connection refused."));
    runFlow("innerNonProxyParamProxyAnotherHost");
    fail("Request should fail as there is no proxy configured");
  }

  @Test
  public void noProxy() throws Exception {
    final BaseEvent event = runFlow("noProxy");
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(OK.getStatusCode()));
    assertThat(event.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }
}
