/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;


import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class HttpRequestCustomTlsConfigTestCase extends AbstractHttpRequestTestCase {

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
    return "http-request-custom-tls-config.xml";
  }

  @Override
  protected boolean enableHttps() {
    return true;
  }

  @Test
  public void configureTlsFromGlobalContext() throws Exception {
    flowRunner("testFlowGlobalContext").withPayload(TEST_MESSAGE).run();
    assertThat(body, equalTo(TEST_MESSAGE));
  }

  @Test
  public void configureTlsFromNestedContext() throws Exception {
    flowRunner("testFlowNestedContext").withPayload(TEST_MESSAGE).run();
    assertThat(body, equalTo(TEST_MESSAGE));
  }

}
