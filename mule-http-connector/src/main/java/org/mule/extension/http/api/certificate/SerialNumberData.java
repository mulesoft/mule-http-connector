/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

import java.math.BigInteger;

/**
 * A custom Data Transfer Object (DTO) to replace the {@link java.security.cert.Certificate} SerialNumber class.
 * <p>
 * This class provides a representation of a serial number used in certificates.
 * </p>
 */
public class SerialNumberData {

  private BigInteger serialNumber;

  /**
   * Constructs a new {@code SerialNumberData} instance with the specified serial number.
   *
   * @param serialNumber the serial number
   */
  public SerialNumberData(BigInteger serialNumber) {
    this.serialNumber = serialNumber;
  }

  /**
   * Returns the serial number.
   *
   * @return the serial number
   */
  public BigInteger getSerialNumber() {
    return serialNumber;
  }
}
