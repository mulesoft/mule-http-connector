/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static java.lang.String.format;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.REQUEST_URI_TOO_LONG;
import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.appendQueryParam;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;

public class HttpListenerHeaderSizeTestCase extends AbstractHttpTestCase {

  private static final int SIZE_DELTA = 1000;

  @Rule
  public SystemProperty maxHeaderSectionSizeSystemProperty =
      new SystemProperty(SYSTEM_PROPERTY_PREFIX + "http.headerSectionSize", "10000");
  @Rule
  public DynamicPort dynamicPort = new DynamicPort("port");

  @Override
  protected String getConfigFile() {
    return "http-listener-max-header-size-config.xml";
  }

  @Test
  public void maxHeaderSizeExceeded() throws Exception {
    HttpResponse response =
        sendRequestWithQueryParam(Integer.valueOf(maxHeaderSectionSizeSystemProperty.getValue()) + SIZE_DELTA);
    StatusLine statusLine = response.getStatusLine();
    assertThat(statusLine.getStatusCode(), is(REQUEST_URI_TOO_LONG.getStatusCode()));
    assertThat(statusLine.getReasonPhrase(), is(REQUEST_URI_TOO_LONG.getReasonPhrase()));
  }

  @Test
  public void maxHeaderSizeNotExceeded() throws Exception {
    int queryParamSize = Integer.valueOf(maxHeaderSectionSizeSystemProperty.getValue()) - SIZE_DELTA;
    HttpResponse response = sendRequestWithQueryParam(queryParamSize);
    assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
    assertThat((int) response.getEntity().getContentLength(), is(queryParamSize));
  }

  private HttpResponse sendRequestWithQueryParam(int queryParamSize) throws Exception {
    String longQueryParamValue = RandomStringUtils.randomAlphanumeric(queryParamSize);
    String urlWithQueryParameter =
        appendQueryParam(format("http://localhost:%d/", dynamicPort.getNumber()), "longQueryParam", longQueryParamValue);
    return Request.Post(urlWithQueryParameter).execute().returnResponse();
  }
}
