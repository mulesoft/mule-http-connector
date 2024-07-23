/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http;

public class X500PrincipalData {

  private String name;

  public X500PrincipalData(String name) {
    this.name = name;
  }

  public X500PrincipalData(PrincipalData principalData) {
    this.name = principalData.getName();
  }

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
