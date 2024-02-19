/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.extension.http.internal.listener.HttpListener.HTTP_NAMESPACE;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.extension.api.error.MuleErrors.ANY;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_GATEWAY;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.EXPECTATION_FAILED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.FORBIDDEN;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.GATEWAY_TIMEOUT;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.METHOD_NOT_ALLOWED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.MOVED_PERMANENTLY;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.MOVED_TEMPORARILY;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.MULTIPLE_CHOICES;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_ACCEPTABLE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_FOUND;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.NOT_MODIFIED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SEE_OTHER;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.TOO_MANY_REQUESTS;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.UNAUTHORIZED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.ERRORS;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.ERROR_HANDLING;

import static java.lang.String.format;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpError;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.functional.api.flow.FlowRunner;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

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
  public void notMappedStatus() throws Exception {
    verifyErrorWhenReceiving(EXPECTATION_FAILED, "417 not understood", ANY.name(),
                             getErrorMessage(" with status code 417"));
  }

  @Test
  @Issue("MULE-18247")
  @Description("Receive the correct 504 error")
  public void gatewayTimeout() throws Exception {
    verifyErrorWhenReceiving(GATEWAY_TIMEOUT, "504 Gateway Timeout", HttpError.GATEWAY_TIMEOUT.name(),
                             getErrorMessage(": gateway timeout (504)"));
  }

  @Test
  @Issue("MULE-18247")
  @Description("Receive the correct 502 error")
  public void badGateway() throws Exception {
    verifyErrorWhenReceiving(BAD_GATEWAY, "502 Bad Gateway", HttpError.BAD_GATEWAY.name(),
                             getErrorMessage(": bad gateway (502)"));
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
    String errorMessageGrizzly = getErrorMessage(": Connection refused", unusedPort) + " connectivity";
    String errorMessageNetty =
        getErrorMessage(": Connection refused: localhost/127.0.0.1:" + unusedPort.getNumber(), unusedPort) + " connectivity";
    String errorMessageGrizzlyWindows =
        getErrorMessage(": Connection refused: no further information", unusedPort) + " connectivity";

    CoreEvent result = getFlowRunner("handled", unusedPort.getNumber()).run();
    assertThat(result.getMessage(),
               hasPayload(anyOf(equalTo(errorMessageNetty), equalTo(errorMessageGrizzly), equalTo(errorMessageGrizzlyWindows))));
  }

  @Test
  @Ignore("Failing in Netty, investigate...")
  public void errorPayloadStreamIsManaged() throws Exception {
    serverStatus = NOT_FOUND.getStatusCode();
    assertThat(getFlowRunner("streaming", httpPort.getNumber()).keepStreamsOpen().run().getMessage(),
               hasPayload(equalTo(DEFAULT_RESPONSE)));
  }

  @Test
  public void movedPermanently() throws Exception {
    verifyErrorWhenRedirect(MOVED_PERMANENTLY, "301 Moved Permanently");
  }

  @Test
  public void movedTemporarily() throws Exception {
    verifyErrorWhenRedirect(MOVED_TEMPORARILY, "302 Found", MOVED_TEMPORARILY.name(),
                            getErrorMessage("302 Moved Temporarily"));
  }

  @Test
  public void multipleChoices() throws Exception {
    verifyErrorWhenRedirect(MULTIPLE_CHOICES, "300 Multiple Choices");
  }

  @Test
  public void seeOther() throws Exception {
    verifyErrorWhenRedirect(SEE_OTHER, "303 See Other");
  }

  @Test
  public void notModified() throws Exception {
    verifyErrorWhenRedirect(NOT_MODIFIED, "304 Not Modified");
  }

  private String getErrorMessage(String customMessage, DynamicPort port) {
    return format("HTTP GET on resource 'http://localhost:%s/testPath' failed%s.", port.getValue(), customMessage);
  }

  private String getErrorMessage(String customMessage) {
    return getErrorMessage(customMessage, httpPort);
  }

  private void verifyErrorWhenReceiving(HttpStatus status, String expectedMessage) throws Exception {
    verifyErrorWhenReceiving(status, format("%s %s", status.getStatusCode(), status.getReasonPhrase()), status.name(),
                             getErrorMessage(expectedMessage));
  }

  private void verifyErrorWhenReceiving(HttpStatus status, Object expectedResult, String expectedError, String expectedMessage)
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

  void verifyErrorWhenRedirect(HttpStatus status, String expectedMessage) throws Exception {
    verifyErrorWhenRedirect(status, format("%s %s", status.getStatusCode(), status.getReasonPhrase()), status.name(),
                            getErrorMessage(expectedMessage));
  }

  void verifyErrorWhenRedirect(HttpStatus status, Object expectedResult, String expectedError, String expectedMessage)
      throws Exception {
    serverStatus = status.getStatusCode();
    // Hit flow with error handler
    CoreEvent result = getFlowRunner("redirect", httpPort.getNumber()).run();
    HttpResponseAttributes attributes = (HttpResponseAttributes) result.getMessage().getAttributes().getValue();
    assertThat(attributes.getStatusCode() + " " + attributes.getReasonPhrase(), is(expectedResult));
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
