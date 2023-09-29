/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.api.metadata.MediaType.HTML;
import static org.mule.runtime.api.metadata.MediaType.TEXT;
import static org.mule.test.allure.AllureConstants.HttpFeature.HttpStory.MULTIPART;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.http.api.HttpHeaders;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiPartWriter;
import org.junit.Test;

@Story(MULTIPART)
public class HttpRequestInboundPartsTestCase extends AbstractHttpRequestTestCase {

  private static final String HTML_CONTENT = "Test part 2";

  @Override
  protected String getConfigFile() {
    return "http-request-inbound-attachments-config.xml";
  }

  @Test
  @Description("Verifies that parts are received, even considering an unknown type (HTML) and a custom header.")
  public void processInboundAttachments() throws Exception {
    CoreEvent event = flowRunner("requestFlow").withPayload(TEST_MESSAGE).keepStreamsOpen().run();
    String contentType = event.getMessage().getPayload().getDataType().getMediaType().toRfcString();

    assertThat(contentType, startsWith(HTML.toRfcString()));
    assertThat(event.getMessage(), hasPayload(equalTo(HTML_CONTENT)));
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    MultiPartWriter multiPartWriter = new MultiPartWriter(response.getWriter());

    response.setContentType(HttpHeaders.Values.MULTIPART_FORM_DATA + "; boundary=" + multiPartWriter.getBoundary());
    response.setStatus(SC_OK);

    multiPartWriter.startPart(TEXT.toRfcString(), new String[] {"Content-Disposition: form-data; name=\"partName1\"",
        "Custom: myHeader"});
    multiPartWriter.write("Test part 1");
    multiPartWriter.endPart();

    multiPartWriter.startPart(HTML.toRfcString(), new String[] {"Content-Disposition: form-data; filename=\"a.html\""});
    multiPartWriter.write(HTML_CONTENT);
    multiPartWriter.endPart();

    multiPartWriter.close();
  }
}
