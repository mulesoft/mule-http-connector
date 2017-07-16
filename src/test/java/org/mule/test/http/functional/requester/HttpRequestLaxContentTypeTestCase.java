/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.requester;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import org.junit.Rule;
import org.junit.Test;
import ru.yandex.qatools.allure.annotations.Features;

@Features(HTTP_EXTENSION)
public class HttpRequestLaxContentTypeTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort httpPort = new DynamicPort("httpPort");

  @Override
  protected String getConfigFile() {
    return "http-request-lax-content-type-config.xml";
  }

  @Test
  public void sendsInvalidContentTypeOnRequest() throws Exception {
    final String url = String.format("http://localhost:%s/requestClientInvalid", httpPort.getNumber());
    HttpRequest request =
        HttpRequest.builder().uri(url).method(GET).entity(new ByteArrayHttpEntity(TEST_MESSAGE.getBytes())).build();
    final HttpResponse response = httpClient.send(request, RECEIVE_TIMEOUT, false, null);

    assertThat(IOUtils.toString(response.getEntity().getContent()), equalTo("invalidMimeType"));
  }

}
