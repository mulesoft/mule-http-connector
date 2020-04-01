/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.api.error.HttpError.CLIENT_ERROR;
import static org.mule.extension.http.api.error.HttpError.SERVER_ERROR;
import static org.mule.extension.http.internal.listener.HttpListener.HTTP_NAMESPACE;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_GATEWAY;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.EXPECTATION_FAILED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.FORBIDDEN;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.GATEWAY_TIMEOUT;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.HTTP_VERSION_NOT_SUPPORTED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.METHOD_NOT_ALLOWED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_ACCEPTABLE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_FOUND;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.TOO_MANY_REQUESTS;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.UNAUTHORIZED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.ERRORS;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.ERROR_HANDLING;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Rule;
import org.junit.Test;

import org.mule.extension.http.api.error.HttpError;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.functional.api.flow.FlowRunner;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import org.mule.tck.junit4.rule.DynamicPort;

import io.qameta.allure.Stories;
import io.qameta.allure.Story;

@Stories({@Story(ERROR_HANDLING), @Story(ERRORS)})
public class HttpRequestErrorHandlingTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort unusedPort = new DynamicPort("unusedPort");
  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  private int serverStatus = 200;
  private String serverContentType = "text/html";
  private boolean timeout = false;
  private Latch done = new Latch();

  @Override
  protected String getConfigFile() {
    return "http-request-errors-config.xml";
  }

  @Test
  public void badRequest() throws Exception {
    verifyErrorWhenReceiving(BAD_REQUEST, ": bad request (400)");
  }

  @Test
  public void unauthorised() throws Exception {
    verifyErrorWhenReceiving(UNAUTHORIZED, ": unauthorized (401)");
  }

  @Test
  public void forbidden() throws Exception {
    verifyErrorWhenReceiving(FORBIDDEN, ": forbidden (403)");
  }

  @Test
  public void notFound() throws Exception {
    verifyErrorWhenReceiving(NOT_FOUND, format(": not found (404)", httpPort.getValue()));
  }

  @Test
  public void methodNotAllowed() throws Exception {
    verifyErrorWhenReceiving(METHOD_NOT_ALLOWED, ": method not allowed (405)");
  }

  @Test
  public void notAcceptable() throws Exception {
    verifyErrorWhenReceiving(NOT_ACCEPTABLE, ": not acceptable (406)");
  }

  @Test
  public void unsupportedMediaType() throws Exception {
    verifyErrorWhenReceiving(UNSUPPORTED_MEDIA_TYPE, ": media type application/xml not supported (415)");
  }

  @Test
  public void tooManyRequest() throws Exception {
    verifyErrorWhenReceiving(TOO_MANY_REQUESTS, ": too many requests (429)");
  }

  @Test
  public void serverError() throws Exception {
    verifyErrorWhenReceiving(INTERNAL_SERVER_ERROR, ": internal server error (500)");
  }

  @Test
  public void serverUnavailable() throws Exception {
    verifyErrorWhenReceiving(SERVICE_UNAVAILABLE, ": service unavailable (503)");
  }

  @Test
  public void notMappedClientErrorStatus() throws Exception {
    verifyErrorWhenReceiving(EXPECTATION_FAILED, "417 Client Error", CLIENT_ERROR.name(),
                             getErrorMessage(": client error (417)"));
  }

  @Test
  public void gatewayTimeout() throws Exception {
    verifyErrorWhenReceiving(GATEWAY_TIMEOUT, "504 Gateway Timeout", HttpError.GATEWAY_TIMEOUT.name(),
                             getErrorMessage(": gateway timeout (504)"));
  }

  @Test
  public void badGateway() throws Exception {
    verifyErrorWhenReceiving(BAD_GATEWAY, "502 Bad Gateway", HttpError.BAD_GATEWAY.name(),
                             getErrorMessage(": bad gateway (502)"));
  }

  @Test
  public void notMappedServerError() throws Exception {
    verifyErrorWhenReceiving(HTTP_VERSION_NOT_SUPPORTED, "505 Server Error", SERVER_ERROR.name(),
                             getErrorMessage(": server error (505)"));
  }

  @Test
  public void timeout() throws Exception {
    timeout = true;
    CoreEvent result = getFlowRunner("handled", httpPort.getNumber()).run();
    done.release();
    assertThat(result.getMessage(), hasPayload(equalTo(getErrorMessage(": Timeout exceeded") + " timeout")));
  }

  @Test
  public void connectivity() throws Exception {

    String customMessage = ": Connection refused";
    if (SystemUtils.IS_OS_WINDOWS) {
      customMessage = customMessage.concat(": no further information");
    }

    CoreEvent result = getFlowRunner("handled", unusedPort.getNumber()).run();
    assertThat(result.getMessage(),
               hasPayload(equalTo(getErrorMessage(customMessage, unusedPort)
                   + " connectivity")));

  }

  @Test
  public void errorPayloadStreamIsManaged() throws Exception {
    serverStatus = NOT_FOUND.getStatusCode();
    assertThat(getFlowRunner("streaming", httpPort.getNumber()).keepStreamsOpen().run().getMessage(),
               hasPayload(equalTo(DEFAULT_RESPONSE)));
  }

  private String getErrorMessage(String customMessage, DynamicPort port) {
    return format("HTTP GET on resource 'http://localhost:%s/testPath' failed%s.", port.getValue(), customMessage);
  }

  private String getErrorMessage(String customMessage) {
    return getErrorMessage(customMessage, httpPort);
  }

  void verifyErrorWhenReceiving(HttpStatus status, String expectedMessage) throws Exception {
    verifyErrorWhenReceiving(status, format("%s %s", status.getStatusCode(), status.getReasonPhrase()), status.name(),
                             getErrorMessage(expectedMessage));
  }

  void verifyErrorWhenReceiving(HttpStatus status, Object expectedResult, String expectedError, String expectedMessage)
      throws Exception {
    serverStatus = status.getStatusCode();
    // Hit flow with error handler
    CoreEvent result = getFlowRunner("handled", httpPort.getNumber()).run();
    assertThat(result.getMessage().getPayload().getValue(), is(expectedResult));
    // Hit flow that will throw back the error
    if (!expectedError.endsWith("ANY")) {
      this.expectedError.expectErrorType(HTTP_NAMESPACE.toUpperCase(), expectedError);
    }
    this.expectedError.expectMessage(is(expectedMessage));
    getFlowRunner("unhandled", httpPort.getNumber()).run();
  }

  private FlowRunner getFlowRunner(String flowName, int port) {
    return flowRunner(flowName).withVariable("port", port);
  }

  @Override
  protected void writeResponse(HttpServletResponse response) throws IOException {
    if (timeout) {
      try {
        done.await();
      } catch (InterruptedException e) {
        // Do nothing
      }
    }
    response.setContentType(serverContentType);
    response.setStatus(serverStatus);
    response.getWriter().print(DEFAULT_RESPONSE);
  }

}
