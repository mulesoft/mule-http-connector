/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http;

import java.io.Serializable;
import java.math.BigInteger;

public class PublicKeyData implements Serializable {

  private String algorithm;
  private byte[] encoded;
  private BigInteger params;
  private BigInteger modulus;
  private String publicKey;

  public PublicKeyData(String algorithm, byte[] encoded) {
    this.algorithm = algorithm;
    this.encoded = encoded;
  }

  public PublicKeyData(String publicKey, BigInteger modulus, BigInteger params, String algorithm, byte[] encoded) {
    this.publicKey = publicKey;
    this.modulus = modulus;
    this.params = params;
    this.algorithm = algorithm;
    this.encoded = encoded;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public byte[] getEncoded() {
    return encoded;
  }

  public BigInteger getParams() {
    return params;
  }

  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public String toString() {
    return publicKey;
  }

}
