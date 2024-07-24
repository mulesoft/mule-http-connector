/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import static java.util.Objects.hash;

import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.Objects;

/**
 * Represents an HTTP URI Parameter
 * 
 * @since 1.8
 */
public class UriParam implements KeyValuePair {

  /**
   * Represents the Key of this HTTP URI Parameter
   */
  @Parameter
  private String key;

  /**
   * Represents the Value of this HTTP URI Parameter
   */
  @Parameter
  private String value;

  @Override
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof UriParam) {
      UriParam other = (UriParam) obj;
      return Objects.equals(key, other.key) && Objects.equals(value, other.value);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return hash(key, value);
  }
}
