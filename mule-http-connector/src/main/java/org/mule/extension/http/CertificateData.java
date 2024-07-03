/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http;

import java.math.BigInteger;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;

public class CertificateData {

  //todo: 3) add test (refer to HttpListenerCustomTlsConfigTestCase)

  private String type;
  private byte[] encoded;
  private int version;
  private PrincipalData subjectDN;
  private PrincipalData issuerDN;
  private BigInteger serialNumber;
  private Date notBefore;
  private Date notAfter;
  private PublicKeyData publicKey;
  private String sigAlgName;
  private String sigAlgOID;
  private byte[] sigAlgParams;
  private byte[] signature;
  private int basicConstraints;
  private List<String> extendedKeyUsage;
  private boolean[] keyUsage;
  private boolean[] issuerUniqueID;
  private List<AlternativeNameData> subjectAlternativeNames;
  private List<AlternativeNameData> issuerAlternativeNames;
  private List<CertificateExtension> extensions;


  public CertificateData(String type, byte[] encoded) {
    this.type = type;
    this.encoded = encoded;
  }

  public CertificateData(String type, byte[] encoded, int version, PrincipalData subjectDN, PrincipalData issuerDN,
                         BigInteger serialNumber, Date notBefore, Date notAfter, PublicKeyData publicKey, String sigAlgName,
                         String sigAlgOID, byte[] sigAlgParams, byte[] signature, int basicConstraints,
                         List<String> extendedKeyUsage, boolean[] keyUsage, boolean[] issuerUniqueID,
                         List<AlternativeNameData> subjectAlternativeNames, List<AlternativeNameData> issuerAlternativeNames,
                         List<CertificateExtension> extensions) {
    this.type = type;
    this.encoded = encoded;
    this.version = version;
    this.subjectDN = subjectDN;
    this.issuerDN = issuerDN;
    this.serialNumber = serialNumber;
    this.notBefore = notBefore;
    this.notAfter = notAfter;
    this.publicKey = publicKey;
    this.sigAlgName = sigAlgName;
    this.sigAlgOID = sigAlgOID;
    this.sigAlgParams = sigAlgParams;
    this.signature = signature;
    this.basicConstraints = basicConstraints;
    this.extendedKeyUsage = extendedKeyUsage;
    this.keyUsage = keyUsage;
    this.issuerUniqueID = issuerUniqueID;
    this.subjectAlternativeNames = subjectAlternativeNames;
    this.issuerAlternativeNames = issuerAlternativeNames;
    this.extensions = extensions;
  }


  public String getType() {
    return type;
  }

  public byte[] getEncoded() {
    return encoded;
  }

  public String getName() {
    return getSubjectDN().getName();
  }

  public int getVersion() {
    return version;
  }

  public PrincipalData getSubjectDN() {
    return subjectDN;
  }

  public PrincipalData getIssuerDN() {
    return issuerDN;
  }

  public X500PrincipalData getSubjectX500Principal() {
    return new X500PrincipalData(subjectDN);
  }


  public X500PrincipalData getIssuerX500Principal() {
    return new X500PrincipalData(issuerDN);
  }

  public BigInteger getSerialNumber() {
    return serialNumber;
  }

  public SerialNumberData getSerialNumberObject() {
    return new SerialNumberData(serialNumber);
  }

  public Date getNotBefore() {
    return notBefore;
  }

  public Date getNotAfter() {
    return notAfter;
  }

  public PublicKeyData getPublicKey() {
    return publicKey;
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

  public boolean[] getIssuerUniqueID() {
    return issuerUniqueID;
  }
}
