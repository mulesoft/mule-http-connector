/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.ERRORS;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.ERROR_HANDLING;
import org.mule.tck.junit4.rule.DynamicPort;

import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;

@Stories({@Story(ERROR_HANDLING), @Story(ERRORS)})
public class HttpRequestExceptionMessagesTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort unusedPort = new DynamicPort("httpPort");

  @Override
  protected String getConfigFile() {
    return "http-request-exception-messages.xml";
  }

  @Test
  public void errorMessageIsLoadedFromCauseIfNull() throws Exception {
    Exception exception = flowRunner("requesterFlow").runExpectingException();
    assertThat(exception.getMessage(), not(isEmptyOrNullString()));
  }

}
