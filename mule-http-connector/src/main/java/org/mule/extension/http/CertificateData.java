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

  private static final long serialVersionUID = -1585440601605666277L;

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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[\n[\n  Version: V").append(version).append("\n");
    sb.append("  Subject: ").append(subjectDN.toString().replaceAll(",", ", ")).append("\n");
    sb.append("  Signature Algorithm: ").append(sigAlgName).append(", OID = ").append(sigAlgOID).append("\n\n");
    sb.append("  Key:  ").append(publicKey.toString().replaceAll("\n", "\n")).append("\n");
    sb.append("  Validity: [From: ").append(notBefore).append(",\n");
    sb.append("               To: ").append(notAfter).append("]\n");
    sb.append("  Issuer: ").append(issuerDN.toString().replaceAll(",", ", ")).append("\n");
    sb.append("  SerialNumber: [    ").append(serialNumber.toString(16)).append("]\n\n");

    if (!extensions.isEmpty()) {
      sb.append("Certificate Extensions: ").append(extensions.size()).append("\n");
      for (int i = 0; i < extensions.size(); i++) {
        CertificateExtension ext = extensions.get(i);
        sb.append("[").append(i + 1).append("]: ");
        sb.append(ext.toString()).append("\n");
      }
    } else {
      sb.append("  Certificate Extensions: 0\n");
    }

    sb.append("  Algorithm: [").append(sigAlgName).append("]\n");
    sb.append("  Signature:\n").append(formatSignature(signature)).append("\n");
    sb.append("]");

    return sb.toString();
  }

  public static String formatSignature(byte[] signature) {
    StringBuilder sb = new StringBuilder();
    StringBuilder asciiPart = new StringBuilder();

    for (int i = 0; i < signature.length; i++) {
      // Start a new line every 16 bytes
      if (i % 16 == 0) {
        if (i != 0) {
          // Append the ASCII part of the previous line
          sb.append(" ").append(asciiPart).append("\n");
          asciiPart.setLength(0); // Reset the ASCII part for the new line
        }
        // Start a new line with the offset
        sb.append(String.format("%04X: ", i));
      }

      // Append the hex value for the current byte
      sb.append(String.format("%02X ", signature[i]));

      // Build the ASCII representation
      if (signature[i] >= 32 && signature[i] < 125) {
        asciiPart.append((char) signature[i]);
      } else {
        asciiPart.append('.');
      }

      // Add extra space after every 8 bytes for readability
      if ((i + 1) % 8 == 0 && (i + 1) % 16 != 0) {
        sb.append("  "); // Add an extra space to widen the gap
      }
    }

    // Handle the last line if the byte array length is not a multiple of 16
    int remainingBytes = signature.length % 16;
    if (remainingBytes != 0) {
      // Calculate padding needed for hex section
      int padding = (16 - remainingBytes) * 3;
      if (remainingBytes <= 8) {
        padding += 1; // Extra space for lines shorter than 8 bytes
      }
      sb.append(repeatSpace(padding)).append(" ").append(asciiPart);
    } else {
      // Append the ASCII part of the last line
      sb.append(" ").append(asciiPart);
    }
    sb.append("\n"); // Ensure the last line ends with a newline

    return sb.toString();
  }

  private static String repeatSpace(int count) {
    char[] chars = new char[count];
    java.util.Arrays.fill(chars, ' ');
    return new String(chars);
  }

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

}
