/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.matcher;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Applies the given {@link Matcher<Iterable>} to the given {@link Header} collection.
 */
public class HttpResponseHeaderValuesIterableMatcher extends TypeSafeMatcher<HttpResponse> {

  private String headerName;
  private Matcher<Iterable<?>> matcher;
  private Collection<String> headerValuesInCollection;

  public HttpResponseHeaderValuesIterableMatcher(String headerNameGiven, Matcher<Iterable<?>> matcherGiven) {
    headerName = headerNameGiven;
    matcher = matcherGiven;
  }

  @Override
  public boolean matchesSafely(HttpResponse response) {
    Header[] headerValuesInArray = response.getHeaders(headerName);
    headerValuesInCollection = Arrays.stream(headerValuesInArray).map(Header::getValue).collect(toList());
    return matcher.matches(headerValuesInCollection);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("a response that has the header <" + headerName + "> that ").appendDescriptionOf(matcher);
  }

  @Override
  protected void describeMismatchSafely(HttpResponse response, Description mismatchDescription) {
    matcher.describeMismatch(headerValuesInCollection, mismatchDescription);
  }

  @Factory
  public static Matcher<HttpResponse> header(String headerName, Matcher<Iterable<?>> matcher) {
    return new HttpResponseHeaderValuesIterableMatcher(headerName, matcher);
  }
}
