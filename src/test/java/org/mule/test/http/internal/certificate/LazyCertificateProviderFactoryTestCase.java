package org.mule.test.http.internal.certificate;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mule.extension.http.internal.certificate.DefaultLazyCertificateProvider;
import org.mule.extension.http.internal.certificate.LazyCertificateProvider;
import org.mule.extension.http.internal.certificate.LazyCertificateProviderFactory;

import java.security.cert.Certificate;
import java.util.function.Supplier;

import org.junit.Test;

/**
 * Created by luciano.raineri on 10/11/18.
 */
public class LazyCertificateProviderFactoryTestCase {

  private static final Supplier<Certificate> CERTIFICATE_SUPPLIER = () -> null;

  @Test
  public void test() {
    LazyCertificateProvider certificateProvider = LazyCertificateProviderFactory.create(CERTIFICATE_SUPPLIER);
    assertThat(certificateProvider, instanceOf(DefaultLazyCertificateProvider.class));
  }



}
