/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.functional.junit4.matchers.ThatMatcher.that;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.ERROR_HANDLING;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.ERROR_MAPPINGS;

import static java.util.Collections.emptyMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import io.qameta.allure.Description;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;

@Stories({@Story(ERROR_HANDLING), @Story(ERROR_MAPPINGS)})
public class HttpRequestErrorMappingsTestCase extends AbstractHttpTestCase {

  private static final String CONNECT_ERROR_MESSAGE = "Could not connect.";
  private static final String UNMATCHED_ERROR_MESSAGE = "Error.";
  private static final String EXPRESSION_ERROR_MESSAGE = "Bad expression.";

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Override
  protected String getConfigFile() {
    return "http-request-error-handling-config.xml";
  }

  @Test
  @Description("Verifies that an unmapped error is handled as ANY.")
  public void simpleRequest() throws Exception {
    verify("noMapping", UNMATCHED_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via wildcard is handled.")
  public void mappedRequest() throws Exception {
    verify("simpleMapping", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that a mapped error via a custom matcher is handled. ")
  public void matchingMappedRequest() throws Exception {
    verify("complexMapping", CONNECT_ERROR_MESSAGE);
  }

  @Test
  @Description("Verifies that an unmapped error is handled as ANY.")
  public void noMatchingMappedRequest() throws Exception {
    verify("complexMapping", UNMATCHED_ERROR_MESSAGE, this);
  }

  @Test
  @Description("Verifies that each error is correctly handled given an operation with multiple mappings.")
  public void multipleMappingsRequest() throws Exception {
    verify("multipleMappings", EXPRESSION_ERROR_MESSAGE, this);
    verify("multipleMappings", CONNECT_ERROR_MESSAGE);
  }

  private void verify(String flowName, String expectedPayload) throws Exception {
    verify(flowName, expectedPayload, emptyMap());
  }

  private void verify(String flowName, String expectedPayload, Object headers) throws Exception {
    assertThat(flowRunner(flowName).withVariable("headers", headers).run().getMessage(), hasPayload(that(is(expectedPayload))));
  }
}
