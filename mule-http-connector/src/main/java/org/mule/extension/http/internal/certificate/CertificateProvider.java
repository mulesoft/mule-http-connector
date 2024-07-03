/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.certificate;

import org.mule.extension.http.CertificateData;

import java.io.Serializable;
import java.security.cert.Certificate;

/**
 * Stores logic for generating a {@link Certificate} and return it.
 *
 * @since 1.4.0
 */
public interface CertificateProvider extends Serializable {

  /**
   * Compute the logic for obtaining the {@link Certificate} and return it.
   *
   * @return a {@link Certificate}
   */
  CertificateData getCertificate();

}
