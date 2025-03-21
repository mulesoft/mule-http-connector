/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test;

import static java.lang.Boolean.getBoolean;

import static org.bouncycastle.crypto.CryptoServicesRegistrar.setSecureRandom;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.junit.runner.notification.RunListener;

/**
 * Done to avoid the use of md5 by allure. This will only be present in fips testing.
 */
public class AllureJunit4 extends RunListener {

  private static final String FIPS_TESTING_PROPERTY = "mule.fips.testing";

  static {
    if (isFipsEnabled()) {
      setSecureRandom(new BouncyCastleFipsProvider().getDefaultSecureRandom());
    }
  }

  public static boolean isFipsEnabled() {
    return getBoolean(FIPS_TESTING_PROPERTY);
  }

}
