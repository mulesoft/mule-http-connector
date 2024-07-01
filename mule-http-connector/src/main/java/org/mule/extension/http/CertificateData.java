package org.mule.extension.http;

import java.security.cert.CertificateEncodingException;

public class CertificateData {

  private X509CertificateData x509Data;
  private String type;
  private byte[] encoded;

  public CertificateData(X509CertificateData x509Data) {
    this.x509Data = x509Data;
  }

  public CertificateData(String type, byte[] encoded) {
    this.type = type;
    this.encoded = encoded;
  }

  // These are the getters present in java.security.cert.Certificate
  public final String getType() {
    return type;
  }

  public byte[] getEncoded() throws CertificateEncodingException {
    if (x509Data != null) {
      return x509Data.getEncodedInternal();  //todo: getEncodedInternal() returns encoded bytes
    } else {
      throw new CertificateEncodingException("Certificate data is null");
    }
  }

  public PublicKeyData getPublicKey() {
    return x509Data.getPublicKey();
  }

  public X509CertificateData getX509() {
    return x509Data;
  }


}
