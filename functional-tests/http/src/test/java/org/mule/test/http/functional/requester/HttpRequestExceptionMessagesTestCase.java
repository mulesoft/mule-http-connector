/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.ERRORS;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.ERROR_HANDLING;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;

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
    String msg = exception.getMessage();
    assertThat(msg, containsString("HTTP GET on resource 'http://notarealsite.mulesoft:124/fakeresource'"));

    // Using latest grizzly, the message is "Couldn't resolve address"
    // Using latest Netty, the message is "nodename nor servname provided, or not known"
    // Using Reactor Netty, the message is "Failed to resolve..."
    assertThat(msg, anyOf(containsString("UnresolvedAddressException"),
                          containsString("Couldn't resolve address"),
                          containsString("nodename nor servname provided, or not known"),
                          containsString("Failed to resolve")));
  }

}
