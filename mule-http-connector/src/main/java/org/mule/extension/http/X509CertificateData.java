/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.util.Date;
import java.util.List;

public class X509CertificateData {

  private int version;
  private String subjectDN;
  private String issuerDN;
  private BigInteger serialNumber;
  private Date notBefore;
  private Date notAfter;
  private PublicKeyData publicKeyData;
  private String sigAlgName;
  private String sigAlgOID;
  private byte[] sigAlgParams;
  private byte[] signature;
  private int basicConstraints;
  private List<String> extendedKeyUsage;
  private boolean[] keyUsage;
  private List<AlternativeNameData> subjectAlternativeNames;
  private List<AlternativeNameData> issuerAlternativeNames;
  private List<CertificateExtension> extensions;

  // Constructor
  public X509CertificateData(String subjectDN, String issuerDN, BigInteger serialNumber, Date notBefore, Date notAfter,
                             PublicKeyData publicKeyData, String sigAlgName, String sigAlgOID, byte[] sigAlgParams,
                             byte[] signature,
                             int version, int basicConstraints, List<String> extendedKeyUsage, boolean[] keyUsage,
                             List<AlternativeNameData> subjectAlternativeNames, List<AlternativeNameData> issuerAlternativeNames,
                             List<CertificateExtension> extensions) {
    this.subjectDN = subjectDN;
    this.issuerDN = issuerDN;
    this.serialNumber = serialNumber;
    this.notBefore = notBefore;
    this.notAfter = notAfter;
    this.publicKeyData = publicKeyData;
    this.sigAlgName = sigAlgName;
    this.sigAlgOID = sigAlgOID;
    this.sigAlgParams = sigAlgParams;
    this.signature = signature;
    this.version = version;
    this.basicConstraints = basicConstraints;
    this.extendedKeyUsage = extendedKeyUsage;
    this.keyUsage = keyUsage;
    this.subjectAlternativeNames = subjectAlternativeNames;
    this.issuerAlternativeNames = issuerAlternativeNames;
    this.extensions = extensions;
  }

  public PrincipalData getSubjectDN() {
    return new PrincipalData(subjectDN);
  }

  public X500PrincipalData getSubjectX500Principal() {
    return new X500PrincipalData(subjectDN);
  }

  public PrincipalData getIssuerDN() {
    return new PrincipalData(issuerDN);
  }

  public X500PrincipalData getIssuerX500Principal() {
    return new X500PrincipalData(issuerDN);
  }

  public Date getNotBefore() {
    return notBefore;
  }

  public Date getNotAfter() {
    return notAfter;
  }

  public BigInteger getSerialNumber() {
    return serialNumber;
  }

  public SerialNumberData getSerialNumberObject() {
    return new SerialNumberData(serialNumber);
  }

  public PublicKeyData getPublicKey() {
    return publicKeyData;
  }

  public String getSigAlgName() {
    return sigAlgName;
  }

  public String getSigAlgOID() {
    return sigAlgOID;
  }

  public byte[] getSigAlgParams() {
    return sigAlgParams;
  }

  public byte[] getSignature() {
    return signature;
  }

  public int getBasicConstraints() {
    return basicConstraints;
  }

  public List<String> getExtendedKeyUsage() {
    return extendedKeyUsage;
  }

  public boolean[] getKeyUsage() {
    return keyUsage;
  }

  public List<AlternativeNameData> getSubjectAlternativeNames() {
    return subjectAlternativeNames;
  }

  public List<AlternativeNameData> getIssuerAlternativeNames() {
    return issuerAlternativeNames;
  }

  public List<CertificateExtension> getExtensions() {
    return extensions;
  }

  public byte[] getEncodedInternal() throws CertificateEncodingException {
    try {
      // Example: Encode fields into a byte array representation
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      encodeString(baos, subjectDN);
      //todo: other incodings
      return baos.toByteArray();
    } catch (IOException e) {
      throw new CertificateEncodingException("Error encoding certificate data", e);
    }
  }

  // Example methods for encoding various types of data
  private void encodeString(OutputStream os, String data) throws IOException {
    // Example implementation for encoding a String
    if (data != null) {
      byte[] bytes = data.getBytes("UTF-8");
      encodeLength(os, bytes.length);
      os.write(bytes);
    }
  }

  private void encodeLength(OutputStream os, int length) throws IOException {
    // Example implementation for encoding length of data
    if (length < 128) {
      os.write(length);
    } else if (length < 256) {
      os.write(0x81);
      os.write(length);
    } else {
      os.write(0x82);
      os.write((length >> 8) & 0xFF);
      os.write(length & 0xFF);
    }
  }

  // Here we have to adapt Enumeration<> to List<> (or Iterable<>)
  public List<String> getElements() {
    //todo
    return null;
  }

  public String getName() {
    return getSubjectDN().getName(); //todo: double check
  }

  public int getVersion() {
    return version;
  }
}
