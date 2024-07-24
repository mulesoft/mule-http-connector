/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

/**
 * A custom Data Transfer Object (DTO) to replace the {@link java.security.Principal} X509Principal class.
 * <p>
 * This class provides a representation of an X.500 Principal, with attributes such as name.
 * </p>
 */
public class X500PrincipalData {

  private String name;

  /**
   * Constructs a new {@code X500PrincipalData} instance with the specified name.
   *
   * @param name the name of the X.500 Principal
   */
  public X500PrincipalData(String name) {
    this.name = name;
  }

  /**
   * Constructs a new {@code X500PrincipalData} instance using a {@code PrincipalData} object.
   *
   * @param principalData the {@code PrincipalData} object
   */
  public X500PrincipalData(PrincipalData principalData) {
    this.name = principalData.getName();
  }

  /**
   * Returns the name of the X.500 Principal.
   *
   * @return the name of the X.500 Principal
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "X500PrincipalData{name='" + name + "'}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    X500PrincipalData that = (X500PrincipalData) obj;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
