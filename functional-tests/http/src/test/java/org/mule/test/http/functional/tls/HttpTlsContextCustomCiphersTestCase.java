/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.tls;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;

import org.mule.functional.api.exception.ExpectedError;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class HttpTlsContextCustomCiphersTestCase extends AbstractHttpTlsContextTestCase {

  @ClassRule
  public static DynamicPort httpsInternalPort1 = new DynamicPort("internal.port.1");
  @ClassRule
  public static DynamicPort httpsInternalPort2 = new DynamicPort("internal.port.2");
  @ClassRule
  public static DynamicPort httpsInternalPort3 = new DynamicPort("internal.port.3");

  private static final String invalidCipher = "TLS_RSA_WITH_AES_256_CBC_SHA";
  private static final String bothProtocolsOneCipher = "bothProtocolsOneCipher";
  private static final String validProtocolValidCipher = "validProtocolValidCipher";
  private static final String validProtocolInvalidCipher = "validProtocolInvalidCipher";
  private static final String OK_RESPONSE = "ok";
  private static final String ERROR_RESPONSE = "Remotely closed";

  @ClassRule
  public static SystemProperty cipherSuites = new SystemProperty("cipherSuites", invalidCipher);
  @ClassRule
  public static SystemProperty verboseExceptions = new SystemProperty("mule.verbose.exceptions", "true");

  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  @Override
  protected String getConfigFile() {
    return "http-tls-ciphers-config.xml";
  }

  @Test
  public void testBothProtocolsOneCipher() throws Exception {
    assertThat(flowRunner(bothProtocolsOneCipher).keepStreamsOpen().run().getMessage(),
               hasPayload(equalTo(OK_RESPONSE)));
  }

  @Test
  public void testValidProtocolValidCipher() throws Exception {
    assertThat(flowRunner(validProtocolValidCipher).keepStreamsOpen().run().getMessage(),
               hasPayload(equalTo(OK_RESPONSE)));
  }

  @Test
  public void testValidProtocolInvalidCipher() throws Exception {
    expectedError.expectErrorType("HTTP", "CONNECTIVITY");
    expectedError.expectMessage(anyOf(containsString(ERROR_RESPONSE), sslValidationError()));
    flowRunner(validProtocolInvalidCipher).run();
  }
}
