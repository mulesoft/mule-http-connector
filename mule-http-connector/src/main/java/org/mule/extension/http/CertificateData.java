/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.util.Date;
import java.util.List;

public class CertificateData implements Serializable {

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
    return "X.509";
  } //todo: test

  public byte[] getEncoded() throws CertificateEncodingException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {

      oos.writeObject(type);
      oos.writeObject(encoded);
      oos.writeObject(version);
      oos.writeObject(subjectDN != null ? subjectDN : "null");
      oos.writeObject(issuerDN != null ? issuerDN : "null");
      oos.writeObject(serialNumber != null ? serialNumber : "null");
      oos.writeObject(notBefore != null ? notBefore : "null");
      oos.writeObject(notAfter != null ? notAfter : "null");
      oos.writeObject(publicKey != null ? publicKey.getEncoded() : "null");
      oos.writeObject(sigAlgName != null ? sigAlgName : "null");
      oos.writeObject(sigAlgOID != null ? sigAlgOID : "null");
      oos.writeObject(sigAlgParams != null ? sigAlgParams : "null");
      oos.writeObject(signature != null ? signature : "null");
      oos.writeObject(basicConstraints);
      oos.writeObject(extendedKeyUsage != null ? extendedKeyUsage : "null");
      oos.writeObject(keyUsage != null ? keyUsage : "null");
      oos.writeObject(issuerUniqueID != null ? issuerUniqueID : "null");
      oos.writeObject(subjectAlternativeNames != null ? subjectAlternativeNames : "null");
      oos.writeObject(issuerAlternativeNames != null ? issuerAlternativeNames : "null");
      oos.writeObject(extensions != null ? extensions : "null");
      oos.flush();
      return baos.toByteArray();
    } catch (IOException e) {
      throw new CertificateEncodingException("Failed to encode certificate", e);
    }
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
