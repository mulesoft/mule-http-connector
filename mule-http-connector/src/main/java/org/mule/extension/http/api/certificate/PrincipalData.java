/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

/**
 * A custom Data Transfer Object (DTO) to replace the {@link java.security.Principal} class.
 * <p>
 * This class is a simple representation of a principal with a single attribute, the name. It implements
 * {@link java.io.Serializable} to allow its instances to be serialized.
 * </p>
 */
public class PrincipalData implements Serializable {

  private static final Logger LOGGER = getLogger(PrincipalData.class);
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

  /**
   * Extracts and returns the common name (CN) from the principal's name.
   * <p>
   * This method assumes that the name is a distinguished name (DN) string, and attempts to find and return the value of the CN
   * attribute. If the CN attribute is not found, the method returns an empty string.
   * </p>
   *
   * @return the common name (CN) if found, otherwise an empty string
   */
  public String getCommonName() {
    try {
      // Get the subject DN
      String subjectDN = this.name;

      // Regular expression to extract CN
      Pattern pattern = Pattern.compile("CN=([^,]*)");
      Matcher matcher = pattern.matcher(subjectDN);

      if (matcher.find()) {
        return matcher.group(1);
      } else {
        return "";
      }
    } catch (Exception e) {
      LOGGER.warn(e.getMessage());
      return "";
    }
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
