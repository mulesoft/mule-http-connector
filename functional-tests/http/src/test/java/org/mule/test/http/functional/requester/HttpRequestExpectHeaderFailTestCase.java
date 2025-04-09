/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.runtime.http.api.HttpConstants.HttpStatus.EXPECTATION_FAILED;

import static java.lang.System.getProperty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.test.http.functional.AbstractHttpExpectHeaderServerTestCase;
import org.mule.test.http.functional.matcher.HttpMessageAttributesMatchers;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class HttpRequestExpectHeaderFailTestCase extends AbstractHttpExpectHeaderServerTestCase {

  private static final String REQUEST_FLOW_NAME = "requestFlow";

  @Override
  protected String getConfigFile() {
    return "http-request-expect-fail-header-config.xml";
  }

  @Test
  public void handlesExpectationFailedResponse() throws Exception {
    // TODO (W-15666548): Enable this for NETTY too.
    assumeThat(getProperty("mule.http.service.implementation", "GRIZZLY"), is("GRIZZLY"));

    startExpectFailedServer();

    // Set a payload that will fail when consumed. As the server rejects the request after processing
    // the header, the client should not send the body.
    CoreEvent response = flowRunner(REQUEST_FLOW_NAME).withPayload(new InputStream() {

      @Override
      public int read() throws IOException {
        throw new IOException("Payload should not be consumed");
      }
    }).run();

    assertThat((HttpResponseAttributes) response.getMessage().getAttributes().getValue(),
               HttpMessageAttributesMatchers.hasStatusCode(EXPECTATION_FAILED.getStatusCode()));

    stopServer();
  }
}
