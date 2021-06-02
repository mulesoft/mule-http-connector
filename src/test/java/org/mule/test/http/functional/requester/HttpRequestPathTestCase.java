/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Test;
import org.mule.runtime.core.api.event.CoreEvent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;

public class HttpRequestPathTestCase extends AbstractHttpRequestTestCase {

  private WithEncodedCharacetersHandler handler = new WithEncodedCharacetersHandler();

  @Override
  protected String getConfigFile() {
    return "http-request-path-reserved-encoded-config.xml";
  }

  @Test
  public void sendRequestHostWithReservedCharactersInPath() throws Exception {
    CoreEvent response = flowRunner("hostWithReservedCharactersInPath").withPayload(TEST_PAYLOAD).run();
    assertThat(response.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
    assertThat(handler.uri, is("/some%27%24separators"));
  }

  @Override
  protected AbstractHandler createHandler(Server server) {
    return handler;
  }

  private class WithEncodedCharacetersHandler extends AbstractHandler {

    public String uri;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

      uri = baseRequest.getRequestURI();

      handleRequest(baseRequest, request, response);

      baseRequest.setHandled(true);
    }
  }

}
