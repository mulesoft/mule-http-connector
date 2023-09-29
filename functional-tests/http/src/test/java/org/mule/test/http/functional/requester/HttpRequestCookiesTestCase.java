/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.runtime.http.api.HttpHeaders.Names.COOKIE;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Sets;
import org.junit.Test;

public class HttpRequestCookiesTestCase extends AbstractHttpRequestTestCase {

  private static final String COOKIE_ROOT_PATH_LOCAL_DOMAIN = "cookieRootPathLocalDomain";
  private static final String COOKIE_CUSTOM_PATH_LOCAL_DOMAIN = "cookieCustomPathLocalDomain";
  private static final String COOKIE_ROOT_PATH_CUSTOM_DOMAIN = "cookieRootPathCustomDomain";
  private static final String COOKIE_EXPIRED = "cookieExpired";
  private static final String COOKIE_TEST_VALUE = "test";

  private static final String CLIENT_COOKIES_ENABLED_FLOW = "clientCookiesEnabled";
  private static final String CLIENT_COOKIES_DISABLED_FLOW = "clientCookiesDisabled";

  @Override
  protected String getConfigFile() {
    return "http-request-cookies-config.xml";
  }

  @Test
  public void cookiesEnabledForSameDomainAndPath() throws Exception {
    flowRunner(CLIENT_COOKIES_ENABLED_FLOW).withPayload(TEST_MESSAGE).withVariable("path", "/").run();
    assertNoCookiesSent();

    flowRunner(CLIENT_COOKIES_ENABLED_FLOW).withPayload(TEST_MESSAGE).withVariable("path", "/").run();
    assertCookiesSent(COOKIE_ROOT_PATH_LOCAL_DOMAIN);
  }

  @Test
  public void cookiesEnabledForSpecificPath() throws Exception {
    flowRunner(CLIENT_COOKIES_ENABLED_FLOW).withPayload(TEST_MESSAGE).withVariable("path", "/path").run();
    assertNoCookiesSent();

    flowRunner(CLIENT_COOKIES_ENABLED_FLOW).withPayload(TEST_MESSAGE).withVariable("path", "/path").run();
    assertCookiesSent(COOKIE_CUSTOM_PATH_LOCAL_DOMAIN, COOKIE_ROOT_PATH_LOCAL_DOMAIN);
  }

  @Test
  public void cookiesDisabledKeepsNoStateBetweenRequests() throws Exception {

    flowRunner(CLIENT_COOKIES_DISABLED_FLOW).withPayload(TEST_MESSAGE).run();
    assertNoCookiesSent();

    flowRunner(CLIENT_COOKIES_DISABLED_FLOW).withPayload(TEST_MESSAGE).run();
    assertNoCookiesSent();
  }

  private void assertCookiesSent(String... cookies) {
    assertThat(headers.containsKey(COOKIE), is(true));

    Set<String> sentCookies = new HashSet<>(headers.get(COOKIE));
    Set<String> expectedCookies = buildCookiesSet(cookies);

    assertThat(sentCookies, equalTo(expectedCookies));
  }

  private Set<String> buildCookiesSet(String... cookies) {
    if (cookies.length == 0) {
      return Sets.newHashSet();
    }

    StringBuilder sb = new StringBuilder(cookies[0] + "=" + COOKIE_TEST_VALUE);
    for (int i = 1; i < cookies.length; ++i) {
      sb.append("; ").append(cookies[i]).append("=").append(COOKIE_TEST_VALUE);
    }
    return Sets.newHashSet(sb.toString());
  }

  private void assertNoCookiesSent() {
    assertThat(headers.containsKey(COOKIE), is(false));
  }

  @Override
  protected void writeResponse(HttpServletResponse response) throws IOException {
    Cookie cookie = new Cookie(COOKIE_ROOT_PATH_LOCAL_DOMAIN, COOKIE_TEST_VALUE);
    cookie.setPath("/");
    cookie.setDomain(".local");
    response.addCookie(cookie);

    cookie = new Cookie(COOKIE_CUSTOM_PATH_LOCAL_DOMAIN, COOKIE_TEST_VALUE);
    cookie.setPath("/path");
    cookie.setDomain(".local");
    response.addCookie(cookie);

    cookie = new Cookie(COOKIE_ROOT_PATH_CUSTOM_DOMAIN, COOKIE_TEST_VALUE);
    cookie.setPath("/");
    cookie.setDomain("domain");
    response.addCookie(cookie);

    cookie = new Cookie(COOKIE_EXPIRED, COOKIE_TEST_VALUE);
    cookie.setPath("/");
    cookie.setDomain(".local");
    cookie.setMaxAge(0);
    response.addCookie(cookie);

    super.writeResponse(response);
  }
}
