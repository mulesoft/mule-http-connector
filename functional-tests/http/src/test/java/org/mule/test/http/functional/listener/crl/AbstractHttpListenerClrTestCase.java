/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.crl;

import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTlsRevocationTestCase;

import org.junit.Rule;

public abstract class AbstractHttpListenerClrTestCase extends AbstractHttpTlsRevocationTestCase {

  @Rule
  public SystemProperty trustStoreType =
      new SystemProperty("trustStoreType", getDefaultEnvironmentConfiguration().getTrustStoreJCEKSType());

  @Rule
  public SystemProperty keyStoreType =
      new SystemProperty("keyStoreType", getDefaultEnvironmentConfiguration().getKeyStorePKS12Type());

  @Rule
  public SystemProperty password =
      new SystemProperty("password", getDefaultEnvironmentConfiguration().resolveStorePassword("test"));

  @Rule
  public SystemProperty entity1KeyStore = new SystemProperty("entity1KeyStore", getEntity1KeyStore());

  @Rule
  public SystemProperty getCertificateAuthorityEntityPath =
      new SystemProperty("certificateAuthorityEntity", getDefaultEnvironmentConfiguration().getCertificateAuthorityEntity());

  @Rule
  public SystemProperty trustStore = new SystemProperty("trustStore", getDefaultEnvironmentConfiguration().getTrustFileForCrl());

  AbstractHttpListenerClrTestCase(String crlPath, String entityCertified) {
    super("http-listener-tls-revocation-file-config.xml", crlPath, entityCertified);
  }

  void verifyRemotelyClosedCause(Exception e) {
    assertThat(e.getCause(), instanceOf(HttpRequestFailedException.class));
    assertThat(e.getCause().getMessage(), containsString("Remotely closed"));
  }


  private String getEntity1KeyStore() {
    if (isFipsTesting()) {
      return "tls/crl/entity1-fips.bcfks";
    }

    return "tls/crl/entity1";
  }

}
