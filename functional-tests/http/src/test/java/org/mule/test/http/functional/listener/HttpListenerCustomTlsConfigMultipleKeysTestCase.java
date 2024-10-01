/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener;

import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpListenerCustomTlsConfigMultipleKeysTestCase extends AbstractHttpTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Rule
  public SystemProperty serverKeyStoreType =
      new SystemProperty("storeType", getDefaultEnvironmentConfiguration().getTestStoreType());

  @Rule
  public SystemProperty tlsClientServerKeyStore =
      new SystemProperty("tlsClientServerKeyStore", getDefaultEnvironmentConfiguration().getTlsClientKeyStoreWithMultipleKeys());


  @Rule
  public SystemProperty tlsTrustStore =
      new SystemProperty("tlsTrustStore", getDefaultEnvironmentConfiguration().getTestGenericTrustKeyStore());

  @Rule
  public SystemProperty tlsTrustStoreFileWithoutMuleServerCertificate =
      new SystemProperty("tlsTrustStoreFileWithoutMuleServerCertificate",
                         getDefaultEnvironmentConfiguration().getTlsTrustStoreFileWithoutMuleServerCertificate());


  @Override
  protected String getConfigFile() {
    return "http-listener-custom-tls-multiple-keys-config.xml";
  }

  @Test
  public void acceptsConnectionWithValidCertificate() throws Exception {
    CoreEvent event = flowRunner("testFlowClientWithCertificate").withPayload(TEST_MESSAGE).run();
    assertThat(event.getMessage().getPayload().getValue(), equalTo(TEST_MESSAGE));
  }

  @Test
  public void rejectsConnectionWithInvalidCertificate() throws Exception {
    expectedException.expectMessage(anyOf(containsString(J8_262_SSL_ERROR_RESPONSE),
                                          containsString(J8_275_SSL_ERROR_RESPONSE),
                                          containsString(J11_SSL_ERROR_RESPONSE),
                                          containsString(J17_SSL_ERROR_RESPONSE),
                                          containsString(BOUNCY_CASTLE_CERTIFICATE_UNKNOWN_ERROR_MESSAGE)));
    flowRunner("testFlowClientWithoutCertificate").withPayload(TEST_MESSAGE).run();
  }


}
