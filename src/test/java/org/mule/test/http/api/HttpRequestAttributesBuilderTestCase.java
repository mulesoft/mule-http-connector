/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.functional.junit4.matchers.ThrowableMessageMatcher.hasMessage;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(HTTP_EXTENSION)
public class HttpRequestAttributesBuilderTestCase extends AbstractMuleTestCase {

  private HttpRequestAttributesBuilder builder = new HttpRequestAttributesBuilder();

  @Test
  public void headers() {
    assertFailure(() -> builder.headers(null), "HTTP headers cannot be null.");
  }

  @Test
  public void queryParams() {
    assertFailure(() -> builder.queryParams(null), "Query params cannot be null.");
  }

  @Test
  public void uriParams() {
    assertFailure(() -> builder.uriParams(null), "URI params cannot be null.");
  }

  @Test
  public void queryString() {
    assertFailure(() -> builder.queryString(null), "Query string cannot be null.");
  }

  @Test
  public void listenerPath() {
    assertFailure(() -> builder.build(), "Listener path cannot be null.");
  }

  @Test
  public void relativePath() {
    assertFailure(() -> builder
        .listenerPath("api/v2/*")
        .build(), "Relative path cannot be null.");
  }

  @Test
  public void version() {
    assertFailure(() -> builder
        .listenerPath("api/v2/*")
        .relativePath("clients")
        .build(), "HTTP version cannot be null.");
  }

  @Test
  public void scheme() {
    assertFailure(() -> builder
        .listenerPath("api/v2/*")
        .relativePath("clients")
        .version("1.1")
        .build(), "Scheme cannot be null.");
  }

  @Test
  public void method() {
    assertFailure(() -> builder
        .listenerPath("api/v2/*")
        .relativePath("clients")
        .version("1.1")
        .scheme("https")
        .build(), "HTTP method cannot be null.");
  }

  @Test
  public void requestPath() {
    assertFailure(() -> builder
        .listenerPath("api/v2/*")
        .relativePath("clients")
        .version("1.1")
        .scheme("https")
        .method("GET")
        .build(), "Request path cannot be null.");
  }

  @Test
  public void requestUri() {
    assertFailure(() -> builder
        .listenerPath("api/v2/*")
        .relativePath("clients")
        .version("1.1")
        .scheme("https")
        .method("GET")
        .requestPath("/api/v2/clients")
        .build(), "Request URI cannot be null.");
  }

  @Test
  public void localAddress() {
    MultiMap<String, String> params = new MultiMap<>();
    params.put("from", "ITA");

    assertFailure(() -> builder
        .listenerPath("api/v2/*")
        .relativePath("clients")
        .version("1.1")
        .scheme("https")
        .method("GET")
        .requestPath("/api/v2/clients")
        .queryString("from=ITA")
        .queryParams(params)
        .requestUri("api/v2/clients?from=ITA")
        .build(), "Local address cannot be null.");
  }

  @Test
  public void remoteAddress() {
    MultiMap<String, String> params = new MultiMap<>();
    params.put("from", "ITA");

    assertFailure(() -> builder
        .listenerPath("api/v2/*")
        .relativePath("clients")
        .version("1.1")
        .scheme("https")
        .method("GET")
        .requestPath("/api/v2/clients")
        .queryString("from=ITA")
        .queryParams(params)
        .requestUri("api/v2/clients?from=ITA")
        .localAddress("localhost/127.0.0.1:8081")
        .build(), "Remote address cannot be null.");
  }

  @Test
  public void maskedRequestPathIsSet() {
    MultiMap<String, String> params = new MultiMap<>();
    params.put("from", "ITA");

    HttpRequestAttributes attributes = builder.remoteAddress("not_localhost")
        .relativePath("clients")
        .version("1.1")
        .scheme("https")
        .method("GET")
        .queryString("from=ITA")
        .queryParams(params)
        .requestUri("api/v2/clients?from=ITA")
        .localAddress("localhost/127.0.0.1:8081")
        .listenerPath("/api/v2/*")
        .requestPath("/api/v2/clients")
        .build();

    assertThat(attributes.getMaskedRequestPath(), is(equalTo("/clients")));
  }

  private void assertFailure(Runnable closure, String message) {
    try {
      closure.run();
      fail("Expected NullPointerException.");
    } catch (Exception e) {
      assertThat(e, is(instanceOf(NullPointerException.class)));
      assertThat(e, hasMessage(equalTo(message)));
    }
  }

}
