/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.certificate;

import static java.lang.Class.forName;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import org.mule.extension.http.api.certificate.CertificateData;
import org.mule.runtime.api.exception.MuleRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.util.function.Supplier;

/**
 * {@link CertificateProvider} that makes use of "SerializableLazyValue" if present in the classpath to store it's
 * values.
 *
 * @since 1.4.0
 */
public class DefaultCertificateProvider implements CertificateProvider {

  static final String SERIALIZABLE_LAZY_VALUE_CLASS_NAME = "org.mule.runtime.api.util.SerializableLazyValue";
  private static final String GET_METHOD_NAME = "get";

  private static final long serialVersionUID = -4010056097536262602L;

  private static Class SERIALIZABLE_LAZY_VALUE_CLASS;
  private static Method GET;
  private static Constructor SERIALIZABLE_LAZY_VALUE_CONSTRUCTOR;

  private Object serializableLazyValue;

  static {
    try {
      SERIALIZABLE_LAZY_VALUE_CLASS = forName(SERIALIZABLE_LAZY_VALUE_CLASS_NAME);
      GET = SERIALIZABLE_LAZY_VALUE_CLASS.getDeclaredMethod(GET_METHOD_NAME);
      SERIALIZABLE_LAZY_VALUE_CONSTRUCTOR = SERIALIZABLE_LAZY_VALUE_CLASS.getConstructor(Supplier.class);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      throw new RuntimeException("Exception while trying to load " + SERIALIZABLE_LAZY_VALUE_CLASS_NAME + " by reflection", e);
    }
  }

  /**
   * Returns a new {@link DefaultCertificateProvider}.
   * Constructor is package private to only allow {@link CertificateProviderFactory} to create instances.
   *
   * @param certificateSupplier actual supplier that returns the {@link Certificate}
   */
  DefaultCertificateProvider(Supplier<CertificateData> certificateSupplier)
      throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    this.serializableLazyValue = SERIALIZABLE_LAZY_VALUE_CONSTRUCTOR.newInstance(certificateSupplier);
  }

  @Override
  public CertificateData getCertificate() {
    try {
      return (CertificateData) GET.invoke(this.serializableLazyValue);
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new MuleRuntimeException(createStaticMessage("Exception while calling method by reflection"), e);
    }
  }

}
