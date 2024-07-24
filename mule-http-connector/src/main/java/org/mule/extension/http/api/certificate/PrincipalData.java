/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

import java.io.Serializable;

/**
 * A custom Data Transfer Object (DTO) to replace the {@link java.security.Principal} class.
 * <p>
 * This class is a simple representation of a principal with a single attribute, the name.
 * It implements {@link java.io.Serializable} to allow its instances to be serialized.
 * </p>
 */
public class PrincipalData implements Serializable {

  private String name;

  /**
   * Constructs a new {@code PrincipalData} instance with the specified name.
   *
   * @param name the name of the principal
   */
  public PrincipalData(String name) {
    this.name = name;
  }

  /**
   * Returns the name of the principal.
   *
   * @return the name of the principal
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    PrincipalData that = (PrincipalData) obj;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
