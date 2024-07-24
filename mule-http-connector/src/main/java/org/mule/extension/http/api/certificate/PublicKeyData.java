/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * A custom Data Transfer Object (DTO) to replace the {@link java.security.PublicKey} class.
 * <p>
 * This class provides a representation of a public key with various attributes such as
 * algorithm, encoded form, parameters, modulus, and the public key string. It implements
 * {@link java.io.Serializable} to allow its instances to be serialized.
 * </p>
 */
public class PublicKeyData implements Serializable {

  private String algorithm;
  private byte[] encoded;
  private BigInteger params;
  private BigInteger modulus;
  private String publicKey;

  /**
   * Constructs a new {@code PublicKeyData} instance with the specified algorithm and encoded form.
   *
   * @param algorithm the algorithm of the public key
   * @param encoded   the encoded form of the public key
   */

  public PublicKeyData(String algorithm, byte[] encoded) {
    this.algorithm = algorithm;
    this.encoded = encoded;
  }

  /**
   * Constructs a new {@code PublicKeyData} instance with detailed attributes.
   *
   * @param publicKey the public key string
   * @param modulus   the modulus of the public key
   * @param params    the parameters of the public key
   * @param algorithm the algorithm of the public key
   * @param encoded   the encoded form of the public key
   */
  public PublicKeyData(String publicKey, BigInteger modulus, BigInteger params, String algorithm, byte[] encoded) {
    this.publicKey = publicKey;
    this.modulus = modulus;
    this.params = params;
    this.algorithm = algorithm;
    this.encoded = encoded;
  }

  /**
   * Returns the algorithm of the public key.
   *
   * @return the algorithm of the public key
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * Returns the encoded form of the public key.
   *
   * @return the encoded form of the public key
   */
  public byte[] getEncoded() {
    return encoded;
  }

  /**
   * Returns the parameters of the public key.
   *
   * @return the parameters of the public key
   */
  public BigInteger getParams() {
    return params;
  }

  /**
   * Returns the modulus of the public key.
   *
   * @return the modulus of the public key
   */
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public String toString() {
    return publicKey;
  }

}
