/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

import java.io.Serializable;

/**
 * A custom Data Transfer Object (DTO) to replace the {@link java.security.cert.X509Certificate} AlternativeName class.
 * <p>
 * This class is a simple representation of an alternative name with two attributes: type and name. It implements
 * {@link java.io.Serializable} to allow its instances to be serialized.
 * </p>
 */
public class AlternativeNameData implements Serializable {

  private int type;
  private String name;

  /**
   * Constructs a new {@code AlternativeNameData} instance with the specified type and name.
   *
   * @param type the type of the alternative name
   * @param name the alternative name
   */
  public AlternativeNameData(int type, String name) {
    this.type = type;
    this.name = name;
  }

  /**
   * Returns the type of the alternative name.
   *
   * @return the type of the alternative name
   */
  public int getType() {
    return type;
  }

  /**
   * Returns the alternative name.
   *
   * @return the alternative name
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return type + ": " + name;
  }
}
