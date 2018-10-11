package org.mule.extension.http.internal.certificate;

import static java.lang.Class.forName;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import org.mule.runtime.api.exception.MuleRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.util.function.Supplier;

/**
 * {@link LazyCertificateProvider} that makes use of "SerializableLazyValue" if present in the classpath to store it's
 * values.
 *
 * @since 1.4.0
 */
public class DefaultLazyCertificateProvider implements LazyCertificateProvider {

  public static final String SERIALIZABLE_LAZY_VALUE_CLASS_NAME = "org.mule.runtime.api.util.SerializableLazyValue";
  private static final String GET_METHOD_NAME = "get";

  private static final long serialVersionUID = -4010056097536262602L;

  private static Class SERIALIZABLE_LAZY_VALUE_CLASS;
  private static Method GET;

  private Object serializableLazyValue;

  static {
    try {
      SERIALIZABLE_LAZY_VALUE_CLASS = forName(SERIALIZABLE_LAZY_VALUE_CLASS_NAME);
      GET = SERIALIZABLE_LAZY_VALUE_CLASS.getDeclaredMethod(GET_METHOD_NAME);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      throw new RuntimeException("Exception while trying to load " + SERIALIZABLE_LAZY_VALUE_CLASS_NAME + " by reflection", e);
    }
  }

  /**
   * Returns a new {@link DefaultLazyCertificateProvider}.
   * Constructor is package private to only allow {@link LazyCertificateProviderFactory} to create instances.
   *
   * @param certificateSupplier actual supplier that returns the {@link Certificate}
   */
  DefaultLazyCertificateProvider(Supplier<Certificate> certificateSupplier) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
    this.serializableLazyValue = SERIALIZABLE_LAZY_VALUE_CLASS.getConstructor(Supplier.class).newInstance(certificateSupplier);
  }

  @Override
  public Certificate getCertificate() {
    try {
      return (Certificate) GET.invoke(this.serializableLazyValue);
    }catch (InvocationTargetException | IllegalAccessException e) {
      throw new MuleRuntimeException(createStaticMessage("Exception while calling method by reflection"), e);
    }
  }

}
