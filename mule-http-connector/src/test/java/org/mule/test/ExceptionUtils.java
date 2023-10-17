/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ExceptionUtils {

  public static void throwException(String className, String message) throws Exception {
    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
    Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
    Exception exception = (Exception) constructor.newInstance(message);
    throw exception;
  }
}
