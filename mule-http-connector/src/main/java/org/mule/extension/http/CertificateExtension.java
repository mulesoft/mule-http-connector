package org.mule.extension.http;

public class CertificateExtension {
  private String oid;
  private byte[] value;

  public CertificateExtension(String oid, byte[] value) {
    this.oid = oid;
    this.value = value;
  }

  public String getOid() {
    return oid;
  }

  public byte[] getValue() {
    return value;
  }
}

