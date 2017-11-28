/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.crl;

import static org.junit.Assert.fail;

import org.junit.Test;

public class HttpRequesterClrRevocationOutdatedCrlTestCase extends AbstractHttpRequesterClrTestCase {

  public HttpRequesterClrRevocationOutdatedCrlTestCase(String configFile) {
    super(configFile, OUTDATED_CRL_FILE_PATH, ENTITY_CERTIFIED_OUTDATED_CRL_SUB_PATH);
  }

  @Test
  public void testServerCertifiedAndOutdatedCrl() throws Exception {
    try {
      runRevocationTestFlow();
      fail("CertPathValidatorException should have been thrown");
    } catch (Exception e) {
      verifyUndeterminedRevocationException(e);
    }
  }

}
