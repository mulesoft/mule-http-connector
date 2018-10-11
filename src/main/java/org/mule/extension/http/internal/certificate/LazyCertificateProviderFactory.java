package org.mule.extension.http.internal.certificate;

import static org.mule.extension.http.internal.certificate.DefaultLazyCertificateProvider.SERIALIZABLE_LAZY_VALUE_CLASS_NAME;
import static org.mule.runtime.core.api.util.ClassUtils.isClassOnPath;
import static org.slf4j.LoggerFactory.getLogger;

import java.security.cert.Certificate;
import java.util.function.Supplier;

import org.slf4j.Logger;

/**
 * Factory class responsible for creating the correct {@link LazyCertificateProvider} according to the available classes.
 */
public class LazyCertificateProviderFactory {

  private static final Logger LOGGER = getLogger(LazyCertificateProviderFactory.class);
  private static boolean isSerializableLazyValuePresent;

  static {
    isSerializableLazyValuePresent = isClassOnPath(SERIALIZABLE_LAZY_VALUE_CLASS_NAME, LazyCertificateProvider.class);
  }

  public static LazyCertificateProvider create(Supplier<Certificate> certificateSupplier) {
    if(isSerializableLazyValuePresent) {
      try {
        return new DefaultLazyCertificateProvider(certificateSupplier);
      }catch (Exception e) {
        LOGGER.warn("Errors while creating " + SERIALIZABLE_LAZY_VALUE_CLASS_NAME + " by reflection, even when class in on classpath. Defaulting to compatibility implementation");
      }
    }
    return new CompatibilityLazyCertificateProvider(certificateSupplier);
  }



}
