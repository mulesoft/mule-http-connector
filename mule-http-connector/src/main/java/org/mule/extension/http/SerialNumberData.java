package org.mule.extension.http;

import java.math.BigInteger;

public class SerialNumberData {
  private BigInteger serialNumber;

  public SerialNumberData(BigInteger serialNumber) {
    this.serialNumber = serialNumber;
  }

  public BigInteger getSerialNumber() {
    return serialNumber;
  }
}
