/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.matcher;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.mule.runtime.api.util.MultiMap;

import java.util.List;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class MultiMapMatcher {

  public static Matcher<MultiMap<String, String>> isEqual(final Map<String, ? extends List<String>> parameters) {
    return new BaseMatcher<MultiMap<String, String>>() {

      @Override
      public boolean matches(Object o) {
        MultiMap<String, String> multiMap = (MultiMap<String, String>) o;
        assertThat(multiMap.keySet(), hasSize(parameters.size()));
        for (String key : parameters.keySet()) {
          assertThat(multiMap.keySet(),
                     containsInAnyOrder(parameters.keySet().toArray(new String[multiMap.keySet().size()])));
          final List<String> multiKeyValues = parameters.get(key);
          final List<String> multiMapValues = multiMap.getAll(key);
          assertThat(multiMapValues,
                     containsInAnyOrder(multiKeyValues.toArray(new String[multiKeyValues.size()])));
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {}
    };
  }

}
