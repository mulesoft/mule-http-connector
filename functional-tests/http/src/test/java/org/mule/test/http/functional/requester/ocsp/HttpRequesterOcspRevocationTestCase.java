/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.ocsp;

import static java.util.Arrays.asList;

import static org.junit.Assert.fail;

import org.mule.test.runner.RunnerDelegateTo;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class HttpRequesterOcspRevocationTestCase extends AbstractHttpOcspRevocationTestCase {

  public HttpRequesterOcspRevocationTestCase(String configFile, String certAlias) {
    super(configFile, REVOKED_OCSP_LIST, certAlias);
  }


  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {"http-requester-ocsp-revocation-config.xml", null},
        {"http-requester-ocsp-revocation-custom-provider-config.xml", "server"},
        {"http-requester-ocsp-revocation-custom-provider-config.xml", "thirdParty"}
    });
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
