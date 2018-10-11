/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.certificate;

import static org.mule.extension.http.internal.certificate.DefaultCertificateProvider.SERIALIZABLE_LAZY_VALUE_CLASS_NAME;
import static org.mule.runtime.core.api.util.ClassUtils.isClassOnPath;
import static org.slf4j.LoggerFactory.getLogger;

import java.security.cert.Certificate;
import java.util.function.Supplier;

import org.slf4j.Logger;

/**
 * Factory class responsible for creating the correct {@link CertificateProvider} according to the available classes.
 */
public class CertificateProviderFactory {

  private static final Logger LOGGER = getLogger(CertificateProviderFactory.class);
  private static boolean isSerializableLazyValuePresent;

  static {
    isSerializableLazyValuePresent = isClassOnPath(SERIALIZABLE_LAZY_VALUE_CLASS_NAME, CertificateProvider.class);
  }

  public static CertificateProvider create(Supplier<Certificate> certificateSupplier) {
    if (isSerializableLazyValuePresent) {
      try {
        return new DefaultCertificateProvider(certificateSupplier);
      } catch (Exception e) {
        LOGGER.warn("Errors while creating " + SERIALIZABLE_LAZY_VALUE_CLASS_NAME
            + " by reflection, even when class in on classpath. Defaulting to compatibility implementation");
      }
    }
    return new CompatibilityCertificateProvider(certificateSupplier);
  }



}
