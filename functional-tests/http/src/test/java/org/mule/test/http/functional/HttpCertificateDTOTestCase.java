/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.HTTPS;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;

@Story(HTTPS)
@Issue("W-15895906")
public class HttpCertificateDTOTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port1 = new DynamicPort("port1");

  @Rule
  public DynamicPort port2 = new DynamicPort("port2");

  @Rule
  public DynamicPort port3 = new DynamicPort("port3");

  @Rule
  public DynamicPort port4 = new DynamicPort("port4");

  @Rule
  public SystemProperty trustStorePath = new SystemProperty("trustStorePath",
                                                            getDefaultEnvironmentConfiguration().getTestSslCaCerts());

  @Rule
  public SystemProperty keyStorePath = new SystemProperty("keyStorePath",
                                                          getDefaultEnvironmentConfiguration().getTestSslKeyStore());

  @Rule
  public SystemProperty storeType = new SystemProperty("storeType",
                                                       getDefaultEnvironmentConfiguration().getTestStoreType());

  @Rule
  public SystemProperty password = new SystemProperty("password",
                                                      getDefaultEnvironmentConfiguration().getTestStorePassword());


  @Override
  protected String getConfigFile() {
    return "http-custom-dto-config.xml";
  }

  @Test
  public void testSubjectDN() throws Exception {
    final CoreEvent res = flowRunner("testFlowSubjectDNClient")
        .withVariable("port", port1.getNumber())
        .withPayload("data")
        .keepStreamsOpen()
        .run();
    assertThat(res.getMessage(), hasPayload(equalTo("subjectDN common name : OLEKSIYS-W3T")));
  }

  @Test
  public void testSerialNumber() throws Exception {
    final CoreEvent res = flowRunner("testFlowSerialNumberClient")
        .withVariable("port", port2.getNumber())
        .withPayload("data")
        .keepStreamsOpen()
        .run();
    assertThat(res.getMessage(), hasPayload(equalTo("serial number : 1578451957")));
  }

  @Test
  public void testPublicKey() throws Exception {
    final CoreEvent res = flowRunner("testFlowPublicKeyClient")
        .withVariable("port", port3.getNumber())
        .withPayload("data")
        .keepStreamsOpen()
        .run();
    assertThat(res.getMessage(), hasPayload(equalTo("public key algorithm : RSA")));
  }


  @Test
  public void testVersion() throws Exception {
    final CoreEvent res = flowRunner("testFlowVersionClient")
        .withVariable("port", port4.getNumber())
        .withPayload("data")
        .keepStreamsOpen()
        .run();
    assertThat(res.getMessage(), hasPayload(equalTo("version : 3")));
  }

}
