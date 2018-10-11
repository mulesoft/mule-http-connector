package org.mule.extension.http.internal.certificate;


import org.mule.runtime.api.util.LazyValue;

import java.security.cert.Certificate;
import java.util.function.Supplier;

/**
 * {@link LazyCertificateProvider} that handles custom logic to provide serialization compatibility
 *
 * @since 1.4.0
 */
public class CompatibilityLazyCertificateProvider implements LazyCertificateProvider {

  private Certificate certificate;
  private transient LazyValue<Certificate> certificateLazyValue;

  CompatibilityLazyCertificateProvider(Supplier<Certificate> certificateSupplier) {
    this.certificateLazyValue = new LazyValue<>(certificateSupplier);
  }

  @Override
  public Certificate getCertificate() {
    if(certificate == null) {
      certificate = certificateLazyValue.get();
    }
    return certificate;
  }
}
