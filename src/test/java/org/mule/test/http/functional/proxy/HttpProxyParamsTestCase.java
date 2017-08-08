package org.mule.test.http.functional.proxy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.runtime.core.api.Event;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.isA;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;

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
    final Event event = runFlow("nonProxyParamProxy");
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(SC_OK));
    assertThat(event.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }

  @Test
  public void innerProxyWithNonProxyHostsParam() throws Exception {
    final Event event = runFlow("innerNonProxyParamProxy");
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(SC_OK));
    assertThat(event.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }

  @Test
  public void proxyWithMultipleHostsNonProxyHostsParam() throws Exception {
    final Event event = runFlow("innerNonProxyParamProxyMultipleHosts");
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(SC_OK));
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
    final Event event = runFlow("noProxy");
    assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(), is(SC_OK));
    assertThat(event.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }
}
