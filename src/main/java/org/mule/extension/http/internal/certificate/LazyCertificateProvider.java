package org.mule.extension.http.internal.certificate;

import java.io.Serializable;
import java.security.cert.Certificate;

/**
 *
 */
public interface LazyCertificateProvider extends Serializable {

  /**
   * Compute the logic for obtaining the {@link Certificate} and return it.
   * @return a {@link Certificate}
   */
  Certificate getCertificate();

}
