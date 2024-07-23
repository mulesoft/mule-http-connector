/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http;

import java.io.Serializable;

public class CertificateExtension implements Serializable {

  private String oid;
  private byte[] value;
  private boolean criticality;
  private String subjectAlternativeName;

  public CertificateExtension(String oid, boolean criticality, byte[] value, String subjectAlternativeName) {
    this.oid = oid;
    this.criticality = criticality;
    this.value = value;
    this.subjectAlternativeName = subjectAlternativeName;
  }

  public String getOid() {
    return oid;
  }

  public byte[] getValue() {
    return value;
  }

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

  private String parseSubjectKeyIdentifier(byte[] value) {
    StringBuilder sb = new StringBuilder();
    sb.append("KeyIdentifier [\n");
    sb.append(formatHexAndAscii(value));
    sb.append("]\n");
    return sb.toString();
  }

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
