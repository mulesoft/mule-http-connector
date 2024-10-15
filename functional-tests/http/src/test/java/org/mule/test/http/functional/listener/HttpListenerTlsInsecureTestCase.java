/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.http.functional.listener;

import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

import org.mule.functional.api.exception.ExpectedError;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

/**
 * Sets up two HTTPS servers with regular trust-stores, except one is insecure. Verifies that a request using a certificate not
 * present in the trust-store only works for the insecure server.
 */
public class HttpListenerTlsInsecureTestCase extends AbstractHttpTestCase {

  @Rule
  public DynamicPort port1 = new DynamicPort("port1");

  @Rule
  public DynamicPort port2 = new DynamicPort("port2");

  @Rule
  public SystemProperty sslCacerts = new SystemProperty("sslCacerts", getDefaultEnvironmentConfiguration().getTestSslCaCerts());

  @Rule
  public SystemProperty sslTestKeyStore =
      new SystemProperty("sslTestKeyStore", getDefaultEnvironmentConfiguration().getTestSslKeyStore());

  @Rule
  public SystemProperty serverKeyStore =
      new SystemProperty("serverKeyStore", getDefaultEnvironmentConfiguration().getTestServerKeyStore());

  @Rule
  public SystemProperty password =
      new SystemProperty("password", getDefaultEnvironmentConfiguration().resolveStorePassword("changeit"));

  @Rule
  public SystemProperty storeType = new SystemProperty("storeType", getDefaultEnvironmentConfiguration().getTestStoreType());


  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  @Override
  protected String getConfigFile() {
    return "http-listener-insecure-config.xml";
  }

  @Test
  public void acceptsInvalidCertificateIfInsecure() throws Exception {
    final CoreEvent res = flowRunner("testRequestToInsecure")
        .withPayload(TEST_PAYLOAD)
        .withVariable("port", port1.getNumber())
        .run();
    assertThat(res.getMessage().getPayload().getValue(), is(TEST_PAYLOAD));
  }

  @Test
  public void rejectsInvalidCertificateIfSecure() throws Exception {
    expectedError.expectCause(instanceOf(IOException.class));
    expectedError.expectCause(hasMessage(containsString("Remotely close")));
    flowRunner("testRequestToSecure")
        .withPayload("data")
        .withVariable("port", port2.getNumber())
        .run();
  }
}
