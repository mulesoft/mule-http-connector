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
