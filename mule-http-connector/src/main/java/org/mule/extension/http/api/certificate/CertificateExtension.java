/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

import java.io.Serializable;

/**
 * A custom Data Transfer Object (DTO) to replace the certificate extension class from the
 * {@link java.security.cert.X509Extension} package.
 * <p>
 * This class encapsulates details of a certificate extension, including its OID, value, criticality, and an optional subject
 * alternative name. It implements {@link java.io.Serializable} to allow its instances to be serialized.
 * </p>
 */
public class CertificateExtension implements Serializable {

  private String oid;
  private byte[] value;
  private boolean criticality;
  private String subjectAlternativeName;

  /**
   * Constructs a new {@code CertificateExtension} instance with the specified attributes.
   *
   * @param oid                    the OID of the extension
   * @param criticality            the criticality of the extension
   * @param value                  the value of the extension
   * @param subjectAlternativeName the subject alternative name (if applicable)
   */
  public CertificateExtension(String oid, boolean criticality, byte[] value, String subjectAlternativeName) {
    this.oid = oid;
    this.criticality = criticality;
    this.value = value;
    this.subjectAlternativeName = subjectAlternativeName;
  }

  /**
   * Returns the OID of the extension.
   *
   * @return the OID of the extension
   */
  public String getOid() {
    return oid;
  }

  /**
   * Returns the value of the extension.
   *
   * @return the value of the extension
   */
  public byte[] getValue() {
    return value;
  }

  /**
   * Returns the criticality of the extension.
   *
   * @return the criticality of the extension
   */
  public boolean getCriticality() {
    return criticality;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ObjectId: ").append(getOid()).append(" Criticality=").append(getCriticality()).append("\n");

    switch (oid) {
      case "2.5.29.17": // Subject Alternative Name
        sb.append("SubjectAlternativeName [\n");
        sb.append(subjectAlternativeName);
        sb.append("]\n");
        break;
      case "2.5.29.14": // Subject Key Identifier
        sb.append("SubjectKeyIdentifier [\n");
        sb.append(parseSubjectKeyIdentifier(value));
        sb.append("]\n");
        sb.append("\n]");
        break;
      default:
        sb.append("Unknown Extension Type\n");
        break;
    }

    return sb.toString();
  }

  /**
   * Parses the Subject Key Identifier from the given byte array.
   *
   * @param value the byte array containing the Subject Key Identifier
   * @return a string representation of the Subject Key Identifier
   */
  private String parseSubjectKeyIdentifier(byte[] value) {
    StringBuilder sb = new StringBuilder();
    sb.append("KeyIdentifier [\n");
    sb.append(formatHexAndAscii(value));
    sb.append("]\n");
    return sb.toString();
  }

  /**
   * Formats the given byte array into a hex and ASCII string representation.
   *
   * @param value the byte array to format
   * @return a formatted string representation of the byte array
   */
  public static String formatHexAndAscii(byte[] value) {
    StringBuilder sb = new StringBuilder();
    int lineLength = 16;

    for (int i = 0; i < value.length; i += lineLength) {
      // Hex address
      sb.append(String.format("%04X: ", i));

      // Hex values
      for (int j = 0; j < lineLength; j++) {
        if (i + j < value.length) {
          sb.append(String.format("%02X ", value[i + j]));
        } else {
          sb.append("   "); // Align if less than 16 bytes
        }
        if (j == 7) {
          sb.append("  "); // Extra space in the middle
        }
      }

      sb.append(" "); // Space before ASCII representation

      // ASCII representation
      for (int j = 0; j < lineLength; j++) {
        if (i + j < value.length) {
          byte b = value[i + j];
          if (b >= 32 && b <= 126) { // Printable ASCII
            sb.append((char) b);
          } else {
            sb.append('.'); // Non-printable
          }
        } else {
          break; // Stop appending spaces if no more bytes
        }
      }

      sb.append("\n");
    }
    return sb.toString();
  }

}
