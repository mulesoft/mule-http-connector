/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A custom Data Transfer Object (DTO) to replace {@link java.security.cert.Certificate},
 * {@link java.security.cert.X509Certificate}, and related classes.
 * <p>
 * This class provides a comprehensive representation of a certificate with various attributes
 * such as type, encoded form, version, subject and issuer distinguished names, serial number,
 * validity period, public key, signature algorithm details, and extensions.
 * It implements {@link java.io.Serializable} to allow its instances to be serialized.
 * </p>
 */
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
  private Set<String> criticalOids = new HashSet<>();
  private Set<String> nonCriticalOids = new HashSet<>();
  private boolean hasUnsupportedCriticalExtensions;

  /**
   * Constructs a new {@code CertificateData} instance with the specified type and encoded form.
   *
   * @param type    the type of the certificate
   * @param encoded the encoded form of the certificate
   */
  public CertificateData(String type, byte[] encoded) {
    this.type = type;
    this.encoded = encoded;
  }

  /**
   * Constructs a new {@code CertificateData} instance with detailed attributes.
   *
   * @param type                        the type of the certificate
   * @param encoded                     the encoded form of the certificate
   * @param version                     the version of the certificate
   * @param subjectDN                   the subject distinguished name
   * @param issuerDN                    the issuer distinguished name
   * @param serialNumber                the serial number of the certificate
   * @param notBefore                   the start date of the validity period
   * @param notAfter                    the end date of the validity period
   * @param publicKey                   the public key of the certificate
   * @param sigAlgName                  the signature algorithm name
   * @param sigAlgOID                   the signature algorithm OID
   * @param sigAlgParams                the signature algorithm parameters
   * @param signature                   the signature
   * @param basicConstraints            the basic constraints
   * @param extendedKeyUsage            the extended key usage
   * @param keyUsage                    the key usage
   * @param issuerUniqueID              the issuer unique ID
   * @param subjectAlternativeNames     the subject alternative names
   * @param issuerAlternativeNames      the issuer alternative names
   * @param extensions                  the certificate extensions
   * @param criticalOids                the critical OIDs
   * @param nonCriticalOids             the non-critical OIDs
   * @param hasUnsupportedCriticalExtensions whether the certificate has unsupported critical extensions
   */
  public CertificateData(String type, byte[] encoded, int version, PrincipalData subjectDN, PrincipalData issuerDN,
                         BigInteger serialNumber, Date notBefore, Date notAfter, PublicKeyData publicKey, String sigAlgName,
                         String sigAlgOID, byte[] sigAlgParams, byte[] signature, int basicConstraints,
                         List<String> extendedKeyUsage, boolean[] keyUsage, boolean[] issuerUniqueID,
                         List<AlternativeNameData> subjectAlternativeNames, List<AlternativeNameData> issuerAlternativeNames,
                         List<CertificateExtension> extensions, Set<String> criticalOids, Set<String> nonCriticalOids,
                         boolean hasUnsupportedCriticalExtensions) {
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
    this.criticalOids = criticalOids;
    this.nonCriticalOids = nonCriticalOids;
    this.hasUnsupportedCriticalExtensions = hasUnsupportedCriticalExtensions;
  }


  // Getter methods with JavaDocs

  /**
   * Returns the type of the certificate.
   *
   * @return the type of the certificate
   */
  public String getType() {
    return "X.509";
  }

  /**
   * Returns the name of the subject distinguished name.
   *
   * @return the name of the subject distinguished name
   */
  public String getName() {
    return getSubjectDN().getName();
  }

  /**
   * Returns the version of the certificate.
   *
   * @return the version of the certificate
   */
  public int getVersion() {
    return version;
  }

  /**
   * Returns the subject distinguished name.
   *
   * @return the subject distinguished name
   */
  public PrincipalData getSubjectDN() {
    return subjectDN;
  }

  /**
   * Returns the issuer distinguished name.
   *
   * @return the issuer distinguished name
   */
  public PrincipalData getIssuerDN() {
    return issuerDN;
  }

  /**
   * Returns the subject X500 principal.
   *
   * @return the subject X500 principal
   */
  public X500PrincipalData getSubjectX500Principal() {
    return new X500PrincipalData(subjectDN);
  }

  /**
   * Returns the issuer X500 principal.
   *
   * @return the issuer X500 principal
   */
  public X500PrincipalData getIssuerX500Principal() {
    return new X500PrincipalData(issuerDN);
  }

  /**
   * Returns the serial number of the certificate.
   *
   * @return the serial number of the certificate
   */
  public BigInteger getSerialNumber() {
    return serialNumber;
  }

  /**
   * Returns the serial number as a SerialNumberData object.
   *
   * @return the serial number as a SerialNumberData object
   */
  public SerialNumberData getSerialNumberObject() {
    return new SerialNumberData(serialNumber);
  }

  /**
   * Returns the start date of the validity period.
   *
   * @return the start date of the validity period
   */
  public Date getNotBefore() {
    return notBefore;
  }

  /**
   * Returns the end date of the validity period.
   *
   * @return the end date of the validity period
   */
  public Date getNotAfter() {
    return notAfter;
  }

  /**
   * Returns the public key of the certificate.
   *
   * @return the public key of the certificate
   */
  public PublicKeyData getPublicKey() {
    return publicKey;
  }

  /**
   * Returns the signature algorithm name.
   *
   * @return the signature algorithm name
   */
  public String getSigAlgName() {
    return sigAlgName;
  }

  /**
   * Returns the signature algorithm OID.
   *
   * @return the signature algorithm OID
   */
  public String getSigAlgOID() {
    return sigAlgOID;
  }

  /**
   * Returns the signature algorithm parameters.
   *
   * @return the signature algorithm parameters
   */
  public byte[] getSigAlgParams() {
    return sigAlgParams;
  }

  /**
   * Returns the signature.
   *
   * @return the signature
   */
  public byte[] getSignature() {
    return signature;
  }

  /**
   * Returns the basic constraints.
   *
   * @return the basic constraints
   */
  public int getBasicConstraints() {
    return basicConstraints;
  }

  /**
   * Returns the extended key usage.
   *
   * @return the extended key usage
   */
  public List<String> getExtendedKeyUsage() {
    return extendedKeyUsage;
  }

  /**
   * Returns the key usage.
   *
   * @return the key usage
   */
  public boolean[] getKeyUsage() {
    return keyUsage;
  }

  /**
   * Returns the subject alternative names.
   *
   * @return the subject alternative names
   */
  public List<AlternativeNameData> getSubjectAlternativeNames() {
    return subjectAlternativeNames;
  }

  /**
   * Returns the issuer alternative names.
   *
   * @return the issuer alternative names
   */
  public List<AlternativeNameData> getIssuerAlternativeNames() {
    return issuerAlternativeNames;
  }

  /**
   * Returns the certificate extensions.
   *
   * @return the certificate extensions
   */
  public List<CertificateExtension> getExtensions() {
    return extensions;
  }

  /**
   * Returns the value of the extension with the specified OID.
   *
   * @param oid the OID of the extension
   * @return the value of the extension
   */
  public byte[] getExtensionValue(String oid) {
    for (CertificateExtension ext : extensions) {
      if (ext.getOid().equals(oid)) {
        return ext.getValue();
      }
    }
    throw new IllegalArgumentException("Extension with OID " + oid + " not found");
  }

  /**
   * Returns the critical extension OIDs.
   *
   * @return the critical extension OIDs
   */
  public Set<String> getCriticalExtensionOIDs() {
    return new HashSet<>(criticalOids);
  }

  /**
   * Returns the non-critical extension OIDs.
   *
   * @return the non-critical extension OIDs
   */
  public Set<String> getNonCriticalExtensionOIDs() {
    return new HashSet<>(nonCriticalOids);
  }

  /**
   * Returns whether the certificate has unsupported critical extensions.
   *
   * @return {@code true} if the certificate has unsupported critical extensions; {@code false} otherwise
   */
  public boolean hasUnsupportedCriticalExtension() {
    return hasUnsupportedCriticalExtensions;
  }

  /**
   * Returns the issuer unique ID.
   *
   * @return the issuer unique ID
   */
  public boolean[] getIssuerUniqueID() {
    return issuerUniqueID;
  }

  /**
   * Checks if the certificate is currently valid.
   *
   * @throws CertificateExpiredException   if the certificate has expired
   * @throws CertificateNotYetValidException if the certificate is not yet valid
   */
  public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
    Date now = new Date();
    checkValidity(now);
  }

  /**
   * Checks if the certificate is valid at the specified date.
   *
   * @param date the date to check the validity against
   * @throws CertificateExpiredException   if the certificate has expired
   * @throws CertificateNotYetValidException if the certificate is not yet valid
   */
  public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
    if (date.before(notBefore)) {
      throw new CertificateNotYetValidException("Certificate is not valid yet: " + date);
    }
    if (date.after(notAfter)) {
      throw new CertificateExpiredException("Certificate has expired: " + date);
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CertificateData)) {
      return false;
    }
    try {
      byte[] thisCert = getEncoded();
      byte[] otherCert = ((CertificateData) other).getEncoded();

      return Arrays.equals(thisCert, otherCert);
    } catch (CertificateException e) {
      return false;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[\n[\n  Version: V").append(version).append("\n");
    sb.append("  Subject: ").append(subjectDN.toString()).append("\n");
    sb.append("  Signature Algorithm: ").append(sigAlgName).append(", OID = ").append(sigAlgOID).append("\n\n");
    sb.append("  Key:  ").append(publicKey.toString().replaceAll("\n", "\n")).append("\n");
    sb.append("  Validity: [From: ").append(notBefore).append(",\n");
    sb.append("               To: ").append(notAfter).append("]\n");
    sb.append("  Issuer: ").append(issuerDN.toString()).append("\n");
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

  /**
   * Formats the signature bytes into a readable string representation.
   *
   * @param signature the signature bytes
   * @return a formatted string representation of the signature
   */
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

  /**
   * Repeats the space character for the specified count.
   *
   * @param count the number of spaces to repeat
   * @return a string of repeated spaces
   */
  private static String repeatSpace(int count) {
    char[] chars = new char[count];
    java.util.Arrays.fill(chars, ' ');
    return new String(chars);
  }

  /**
   * Returns the encoded form of the certificate.
   *
   * @return the encoded form of the certificate
   * @throws CertificateEncodingException if encoding fails
   */
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
      oos.writeObject(publicKey != null ? publicKey : "null");
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
