/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http;

public class PublicKeyData {

  private String algorithm;
  private byte[] encoded;

  public PublicKeyData(String algorithm, byte[] encoded) {
    this.algorithm = algorithm;
    this.encoded = encoded;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public byte[] getEncoded() {
    return encoded;
  }
}
