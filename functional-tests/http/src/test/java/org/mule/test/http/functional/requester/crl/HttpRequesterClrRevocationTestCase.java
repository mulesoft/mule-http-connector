/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.crl;

import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

import org.junit.BeforeClass;
import org.junit.Test;

public class HttpRequesterClrRevocationTestCase extends AbstractHttpRequesterClrTestCase {

  @BeforeClass
  public static void before() {
    assumeFalse("Check that this is not in fips where the standard revocation check does not work. Another of the documented options should be used",
                isFipsTesting());
  }

  public HttpRequesterClrRevocationTestCase(String configFile) {
    super(configFile, REVOKED_CRL_FILE_PATH, ENTITY_CERTIFIED_REVOCATION_SUB_PATH);
  }

  @Test
  public void testServerCertifiedAndRevoked() throws Exception {
    try {
      runRevocationTestFlow();
      fail("CertificateRevokedException should have been thrown.");
    } catch (Exception e) {
      verifyRevocationException(e);
    }
  }

}
