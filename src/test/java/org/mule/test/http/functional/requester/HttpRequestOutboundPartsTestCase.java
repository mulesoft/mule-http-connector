/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.MediaType.HTML;
import static org.mule.runtime.api.metadata.MediaType.TEXT;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_DISPOSITION;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.MULTIPART;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiPartInputStreamParser;
import org.junit.Rule;
import org.junit.Test;

@Story(MULTIPART)
public class HttpRequestOutboundPartsTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public SystemProperty sendBufferSize = new SystemProperty("sendBufferSize", "128");

  @Override
  protected String getConfigFile() {
    return "http-request-outbound-parts-config.xml";
  }

  private Collection<Part> parts;
  private String requestContentType;

  @Test
  @Description("Verifies that parts are sent, even considering an unknown type (HTML) and a custom header.")
  public void partsAreSent() throws Exception {
    String payload = "<!DOCTYPE html><title>content 2</title>";
    flowRunner("partsFlow").withPayload(payload).withMediaType(HTML).run();

    assertThat(requestContentType, startsWith("multipart/form-data"));
    assertThat(requestContentType, containsString(" boundary="));
    assertThat(parts.size(), equalTo(2));

    assertPart("partOne", TEXT, "content 1");
    assertThat(getPart("partOne").getHeader("Custom"), is("myHeader"));
    assertPart("partTwo", HTML, payload);
    assertFormDataContentDisposition(getPart("partTwo"), "partTwo", "a.html");
  }

  private void assertPart(String name, MediaType expectedContentType, String expectedBody) throws Exception {
    Part part = getPart(name);
    assertThat(part, notNullValue());
    assertThat(part.getContentType(), startsWith(expectedContentType.toString()));
    assertThat(IOUtils.toString(part.getInputStream()), equalTo(expectedBody));
  }

  private void assertFormDataContentDisposition(Part part, String expectedName, String expectedFileName) {
    String expected = String.format("form-data; name=\"%s\"", expectedName);
    if (expectedFileName != null) {
      expected += String.format("; filename=\"%s\"", expectedFileName);
    }

    assertThat(part.getHeader(CONTENT_DISPOSITION), equalTo(expected));
  }

  private Part getPart(String name) {
    for (Part part : parts) {
      if (part.getName().equals(name)) {
        return part;
      }
    }
    return null;
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    requestContentType = request.getHeader(CONTENT_TYPE);

    MultiPartInputStreamParser inputStreamParser =
        new MultiPartInputStreamParser(request.getInputStream(), request.getContentType(), null, null);

    parts = inputStreamParser.getParts();

    response.setContentType(HTML.toString());
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().print(DEFAULT_RESPONSE);
  }
}
