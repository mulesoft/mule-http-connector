/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONNECTION;
import static org.mule.runtime.http.api.HttpHeaders.Values.CLOSE;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import io.qameta.allure.Story;
import org.junit.Test;
import org.junit.Rule;

@Story(HTTPS)
public class HttpRequestTlsConnectionCloseTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public SystemProperty trustStoreFile =
      new SystemProperty("trustStore", getDefaultEnvironmentConfiguration().getTestGenericTrustKeyStore());

  @Rule
  public SystemProperty clientKeystoreFile =
      new SystemProperty("clientKeyStore", getDefaultEnvironmentConfiguration().getTestClientKeyStore());

  @Rule
  public SystemProperty storeType = new SystemProperty("storeType", getDefaultEnvironmentConfiguration().getTestStoreType());

  @Override
  protected String getConfigFile() {
    return "http-request-connection-close-config.xml";
  }

  @Override
  protected boolean enableHttps() {
    return true;
  }

  @Override
  protected void writeResponse(HttpServletResponse response) throws IOException {
    super.writeResponse(response);
    response.addHeader(CONNECTION, CLOSE);
  }

  @Test
  public void handlesRequest() throws Exception {
    CoreEvent response = flowRunner("testFlowHttps").withPayload(AbstractMuleTestCase.TEST_PAYLOAD).run();
    assertThat(response.getMessage(), hasPayload(equalTo((DEFAULT_RESPONSE))));
  }
}
