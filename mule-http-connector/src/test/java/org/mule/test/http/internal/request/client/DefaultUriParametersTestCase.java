/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.internal.request.client;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mule.sdk.api.http.HttpConstants.Protocol.HTTP;
import static org.mule.sdk.api.http.HttpConstants.Protocol.HTTPS;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.internal.request.client.DefaultUriParameters;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Test;

public class DefaultUriParametersTestCase extends AbstractMuleTestCase {

  private static final String HOST = "localhost";
  private static final Integer INVALID_PORT = -703;

  private void assertCorrectParameters(UriParameters uriParameters) {
    assertThat(uriParameters.getHost(), is(equalTo(HOST)));
    if (uriParameters.getScheme().equals(HTTP)) {
      assertThat(uriParameters.getPort(), is(equalTo(HTTP.getDefaultPort())));
    } else {
      assertThat(uriParameters.getPort(), is(equalTo(HTTPS.getDefaultPort())));
    }
  }

  @Test
  public void createUriParametersWithValidPortAndHTTP() throws Exception {
    assertCorrectParameters(new DefaultUriParameters(HTTP, HOST, 80));
  }

  @Test
  public void createUriParametersWithValidPortAndHTTPS() throws Exception {
    assertCorrectParameters(new DefaultUriParameters(HTTPS, HOST, 443));
  }

  @Test
  public void createUriParametersWithNullPortAndHTTP() throws Exception {
    assertCorrectParameters(new DefaultUriParameters(HTTP, HOST, null));
  }

  @Test
  public void createUriParametersWithNullPortAndHTTPS() throws Exception {
    assertCorrectParameters(new DefaultUriParameters(HTTPS, HOST, null));
  }

  @Test
  public void createUriParametersWithInvalidPortAndHTTP() throws Exception {
    assertCorrectParameters(new DefaultUriParameters(HTTP, HOST, INVALID_PORT));
  }

  @Test
  public void createUriParametersWithInvalidPortAndHTTPS() throws Exception {
    assertCorrectParameters(new DefaultUriParameters(HTTPS, HOST, INVALID_PORT));
  }

}
