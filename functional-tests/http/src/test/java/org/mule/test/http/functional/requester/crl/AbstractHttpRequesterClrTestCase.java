/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.crl;

import static java.util.Arrays.asList;

import static org.mule.test.http.functional.fips.DefaultTestConfiguration.getDefaultEnvironmentConfiguration;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;

import static org.junit.runners.Parameterized.Parameters;

import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.http.functional.AbstractHttpTlsRevocationTestCase;
import org.mule.test.runner.RunnerDelegateTo;

import java.util.Collection;

import org.junit.Rule;
import org.junit.runners.Parameterized;


@RunnerDelegateTo(Parameterized.class)
public abstract class AbstractHttpRequesterClrTestCase extends AbstractHttpTlsRevocationTestCase {

  @Rule
  public SystemProperty trustStoreType =
      new SystemProperty("trustStoreType", getDefaultEnvironmentConfiguration().getTrustStoreJCEKSType());

  @Rule
  public SystemProperty trustStore =
      new SystemProperty("truststore", getDefaultEnvironmentConfiguration().getTrustFileForCrl());

  @Rule
  public SystemProperty keyStoreType =
      new SystemProperty("keyStoreType", getDefaultEnvironmentConfiguration().getKeyStorePKS12Type());

  @Rule
  public SystemProperty keyStore =
      new SystemProperty("keystore", getEntity1KeyStore());

  @Rule
  public SystemProperty storeType =
      new SystemProperty("keyStoreType", getDefaultEnvironmentConfiguration().getKeyStorePKS12Type());


  @Rule
  public SystemProperty certificationAuthority =
      new SystemProperty("certificationAuthority", getDefaultEnvironmentConfiguration().getCertificateAuthorityEntity());


  @Rule
  public SystemProperty password =
      new SystemProperty("password", getDefaultEnvironmentConfiguration().resolveStorePassword("test"));

  protected AbstractHttpRequesterClrTestCase(String configFile, String crlPath, String entityCertified) {
    super(configFile, crlPath, entityCertified);
  }

  @Parameters
  public static Collection<Object> data() {
    if (isFipsTesting()) {
      return asList(new Object[] {
          "http-requester-tls-revocation-file-config.xml"
      });
    } else {
      return asList(new Object[] {
          "http-requester-tls-revocation-file-config.xml",
          "http-requester-tls-revocation-crl-standard-config.xml"
      });
    }
  }

  @Override
  public boolean addToolingObjectsToRegistry() {
    return true;
  }

  private String getEntity1KeyStore() {
    if (isFipsTesting()) {
      return "tls/crl/entity1-fips.bcfks";
    }

    return "tls/crl/entity1";
  }
}
