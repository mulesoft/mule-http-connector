/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;


import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.HTTPS;
import org.mule.runtime.core.api.event.CoreEvent;

import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Story(HTTPS)
public class HttpRequestValidateCertificateTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Override
  protected String getConfigFile() {
    return "http-request-validate-certificate-config.xml";
  }

  @Override
  protected boolean enableHttps() {
    return true;
  }

  @Test
  public void rejectsMissingCertificate() throws Exception {
    expectedException.expectMessage(anyOf(containsString(J8_262_SSL_ERROR_RESPONSE),
                                          containsString(J8_275_SSL_ERROR_RESPONSE_1),
                                          containsString(J8_275_SSL_ERROR_RESPONSE_2),
                                          containsString(J11_SSL_ERROR_RESPONSE)));
    flowRunner("missingCertFlow").withPayload(TEST_MESSAGE).run();
  }

  @Test
  public void acceptsValidCertificate() throws Exception {
    CoreEvent result = flowRunner("validCertFlow").withPayload(TEST_MESSAGE).run();
    assertThat(result.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
  }
}
