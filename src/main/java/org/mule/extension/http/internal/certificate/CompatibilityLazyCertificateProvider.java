package org.mule.extension.http.internal.certificate;


import org.mule.runtime.api.util.LazyValue;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.cert.Certificate;
import java.util.function.Supplier;

/**
 * {@link LazyCertificateProvider} that handles custom logic to provide serialization compatibility
 *
 * @since 1.4.0
 */
public class CompatibilityLazyCertificateProvider implements LazyCertificateProvider {

  private static final long serialVersionUID = -6620659020113867138L;

  private Certificate certificate;
  private transient LazyValue<Certificate> certificateLazyValue;

  /**
   * Returns a new {@link CompatibilityLazyCertificateProvider}.
   * Constructor is package private to only allow {@link LazyCertificateProviderFactory} to create instances.
   *
   * @param certificateSupplier actual supplier that returns the {@link Certificate}
   */
  CompatibilityLazyCertificateProvider(Supplier<Certificate> certificateSupplier) {
    this.certificateLazyValue = new LazyValue<>(certificateSupplier);
  }

  @Override
  public Certificate getCertificate() {
    if(certificate == null && certificateLazyValue != null) {
      certificate = certificateLazyValue.get();
    }
    return certificate;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    getCertificate();
    out.defaultWriteObject();
  }
}
