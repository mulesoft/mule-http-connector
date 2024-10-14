/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;


import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.SystemProperty;

import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Story(HTTPS)
public class HttpRequestValidateCertificateTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public SystemProperty trustStoreFile =
      new SystemProperty("trustStore", getDefaultEnvironmentConfiguration().getTestGenericTrustKeyStore());

  @Rule
  public SystemProperty storeType = new SystemProperty("storeType",
                                                       getDefaultEnvironmentConfiguration().getTestStoreType());


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
                                          containsString(J8_275_SSL_ERROR_RESPONSE),
                                          containsString(J11_SSL_ERROR_RESPONSE),
                                          containsString(BOUNCY_CASTLE_CERTIFICATE_UNKNOWN_ERROR_MESSAGE)));
    flowRunner("missingCertFlow").withPayload(TEST_MESSAGE).run();
  }

  @Test
  public void acceptsValidCertificate() throws Exception {
    CoreEvent result = flowRunner("validCertFlow").withPayload(TEST_MESSAGE).run();
    assertThat(result.getMessage().getPayload().getValue(), equalTo(DEFAULT_RESPONSE));
  }
}
