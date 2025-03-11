/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.certificate;

import static java.lang.System.currentTimeMillis;
import static java.math.BigInteger.valueOf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class CertificateDataTestCase {

  @Test
  public void simpleEqualsContract() {
    EqualsVerifier.simple().forClass(CertificateData.class)
        .withOnlyTheseFields("encoded")
        .verify();
  }

  // tests for AlternativeNameData
  @Test
  public void testAlternativeNameData() {
    AlternativeNameData data = new AlternativeNameData(1, "testName");
    assertEquals(1, data.getType());
    assertEquals("testName", data.getName());
  }

  @Test
  public void alternativeNameDataToString() {
    AlternativeNameData data = new AlternativeNameData(2, "anotherName");
    assertEquals("2: anotherName", data.toString());
  }

  // tests for SerialNumberData
  @Test
  public void testSerialNumberData() {
    BigInteger serialNumber = new BigInteger("12345678901234567890");
    SerialNumberData serialNumberData = new SerialNumberData(serialNumber);
    assertEquals(serialNumber, serialNumberData.getSerialNumber());
  }

  // tests for PublicKeyData
  @Test
  public void testPublicKeyData() {
    String algorithm = "RSA";
    byte[] encoded = {1, 2, 3};
    PublicKeyData publicKeyData = new PublicKeyData(algorithm, encoded);

    assertEquals(algorithm, publicKeyData.getAlgorithm());
    assertArrayEquals(encoded, publicKeyData.getEncoded());
    assertNull(publicKeyData.getParams());
    assertNull(publicKeyData.getModulus());
    assertNull(publicKeyData.getPublicKey());
  }

  @Test
  public void testPublicKeyDataWithFullParameters() {
    String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";
    BigInteger modulus = new BigInteger("123456789");
    BigInteger params = new BigInteger("987654321");
    String algorithm = "DSA";
    byte[] encoded = {4, 5, 6};

    PublicKeyData publicKeyData = new PublicKeyData(publicKey, modulus, params, algorithm, encoded);

    assertEquals(algorithm, publicKeyData.getAlgorithm());
    assertArrayEquals(encoded, publicKeyData.getEncoded());
    assertEquals(params, publicKeyData.getParams());
    assertEquals(modulus, publicKeyData.getModulus());
    assertEquals(publicKey, publicKeyData.getPublicKey());
  }

  // tests for PrincipalData
  @Test
  public void testGetCommonNameWithCN() {
    String name = "CN=Test Name, OU=Test Unit, O=Test Org";
    PrincipalData principalData = new PrincipalData(name);
    assertEquals("Test Name", principalData.getCommonName());
  }

  // tests for PrincipalData
  @Test
  public void testGetCommonNameWithoutCN() {
    String name = "OU=Test Unit, O=Test Org";
    PrincipalData principalData = new PrincipalData(name);
    assertEquals("", principalData.getCommonName());
  }

  @Test
  public void testGetCommonNameWithMultipleCN() {
    String name = "CN=Test Name, OU=Test Unit, CN=Another Name, O=Test Org";
    PrincipalData principalData = new PrincipalData(name);
    assertEquals("Test Name", principalData.getCommonName());
  }

  @Test
  public void testPrincipalDataToString() {
    String name = "CN=Test Name, OU=Test Unit, O=Test Org";
    PrincipalData principalData = new PrincipalData(name);
    assertEquals(name, principalData.toString());
  }

  @Test
  public void testPrincipalDataWithNPEHandled() {
    PrincipalData principalData = new PrincipalData(null);
    // NPE expected here
    String commonName = principalData.getCommonName();
    // exception was handled and an empty string was returned
    assertEquals("", commonName);
  }

  @Test
  public void testX500PrincipalData() {
    String name = "CN=Test Name, OU=Test Unit, O=Test Org";
    X500PrincipalData x500PrincipalData = new X500PrincipalData(name);
    assertEquals(name, x500PrincipalData.getName());
  }

  @Test
  public void testX500PrincipalDataCreatedWithPrincipalData() {
    String name = "CN=Test Name, OU=Test Unit, O=Test Org";
    PrincipalData principalData = new PrincipalData(name);
    X500PrincipalData x500PrincipalData = new X500PrincipalData(principalData);
    assertEquals(name, x500PrincipalData.getName());
  }

  @Test
  public void testX500PrincipalDataToString() {
    String name = "CN=Test Name, OU=Test Unit, O=Test Org";
    X500PrincipalData x500PrincipalData = new X500PrincipalData(name);
    assertEquals("X500PrincipalData{name='" + name + "'}", x500PrincipalData.toString());
  }

  @Test
  public void testX500PrincipalDataEqualsAndHashCode() {
    X500PrincipalData x500PrincipalData1 = new X500PrincipalData("CN=Test Name");
    X500PrincipalData x500PrincipalData2 = new X500PrincipalData("CN=Test Name");
    X500PrincipalData x500PrincipalData3 = new X500PrincipalData("CN=Different Name");

    assertEquals(x500PrincipalData1, x500PrincipalData2);
    assertNotEquals(x500PrincipalData1, x500PrincipalData3);

    assertEquals(x500PrincipalData1.hashCode(), x500PrincipalData2.hashCode());
    assertNotEquals(x500PrincipalData1.hashCode(), x500PrincipalData3.hashCode());
  }

  @Test
  public void testX500PrincipalDataEqualsWithNull() {
    X500PrincipalData x500PrincipalData = new X500PrincipalData("CN=Test Name");
    assertNotEquals(null, x500PrincipalData);
  }

  @Test
  public void testX500PrincipalDataEqualsWithDifferentClass() {
    X500PrincipalData x500PrincipalData = new X500PrincipalData("CN=Test Name");
    assertNotEquals("String Object", x500PrincipalData);
  }

  @Test
  public void testX500PrincipalDataWithEmptyString() {
    X500PrincipalData x500PrincipalData = new X500PrincipalData("");
    assertEquals("", x500PrincipalData.getName());
  }

  @Test
  public void testX500PrincipalDataWithNullName() {
    X500PrincipalData x500PrincipalData = new X500PrincipalData((String) null);
    assertNull(x500PrincipalData.getName());
  }

  @Test
  public void testX500PrincipalDataEqualsWithSameObject() {
    X500PrincipalData x500PrincipalData = new X500PrincipalData("CN=Test Name");
    X500PrincipalData x500PrincipalData2 = new X500PrincipalData("CN=Test Name");
    assertEquals(x500PrincipalData, x500PrincipalData2);
  }

  // tests for CertificateExtension
  @Test
  public void testCertificateExtension() {
    String oid = "2.5.29.17";
    boolean criticality = true;
    byte[] value = {1, 2, 3};
    String subjectAlternativeName = "DNS:example.com";

    CertificateExtension extension = new CertificateExtension(oid, criticality, value, subjectAlternativeName);

    assertEquals(oid, extension.getOid());
    assertEquals(criticality, extension.getCriticality());
    assertArrayEquals(value, extension.getValue());
  }

  @Test
  public void testCertificateExtensionToStringWithSubjectAlternativeName() {
    String oid = "2.5.29.17";
    boolean criticality = true;
    byte[] value = {1, 2, 3};
    String subjectAlternativeName = "DNS:example.com";

    CertificateExtension extension = new CertificateExtension(oid, criticality, value, subjectAlternativeName);

    String expected = "ObjectId: 2.5.29.17 Criticality=true\n" +
        "SubjectAlternativeName [\n" +
        "DNS:example.com]\n";

    assertEquals(expected, extension.toString());
  }

  @Test
  public void testCertificateExtensionToStringWithUnknownExtension() {
    String oid = "1.2.3.4";
    boolean criticality = true;
    byte[] value = {1, 2, 3};
    String subjectAlternativeName = null;

    CertificateExtension extension = new CertificateExtension(oid, criticality, value, subjectAlternativeName);

    String expected = "ObjectId: 1.2.3.4 Criticality=true\n" +
        "Unknown Extension Type\n";

    assertEquals(expected, extension.toString());
  }

  @Test
  public void testCertificateExtensionFormatHexAndAscii() {
    byte[] value = {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21, 0x00, 0x01, 0x02, 0x03, 0x04};
    String formatted = CertificateExtension.formatHexAndAscii(value);

    String expected = "0000: 48 65 6C 6C 6F 20 57 6F   72 6C 64 21 00 01 02 03  Hello World!....\n" +
        "0010: 04                                                 .\n";
    assertEquals(expected, formatted);
  }

  @Test
  public void testCertificateExtensionToStringWithSubjectKeyIdentifier() {
    String oid = "2.5.29.14";
    boolean criticality = false;
    byte[] value = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10};
    String subjectAlternativeName = null;

    CertificateExtension extension = new CertificateExtension(oid, criticality, value, subjectAlternativeName);

    String expected = "ObjectId: 2.5.29.14 Criticality=false\n" +
        "SubjectKeyIdentifier [\n" +
        "KeyIdentifier [\n" +
        "0000: 01 02 03 04 05 06 07 08   09 0A 0B 0C 0D 0E 0F 10  ................\n" + // Hex and ASCII representation
        "]\n" +
        "]\n\n]";
    assertEquals(expected, extension.toString());
  }

  // tests for CertificateData
  @Test
  public void testCertificateDataWithTypeAndEncoded() throws CertificateEncodingException {
    String type = "X.509";
    byte[] encoded = {1, 2, 3};

    CertificateData data = new CertificateData(type, encoded);

    assertEquals(type, data.getType());
  }

  @Test
  public void testCertificateDataGetName() {
    PrincipalData subjectDN = new PrincipalData("CN=Test Subject");
    CertificateData data = new CertificateData("X.509", new byte[] {1, 2, 3}, 1, subjectDN, null, null, null, null, null, null,
                                               null, null, null, 0, null, null, null, null, null, null, null, null, false);
    assertEquals("CN=Test Subject", data.getName());
  }

  @Test
  public void testCertificateDataGetX500Principal() {
    PrincipalData subjectDN = new PrincipalData("CN=Test Subject");
    CertificateData data = new CertificateData("X.509", new byte[] {1, 2, 3}, 1, subjectDN, null, null, null, null, null, null,
                                               null, null, null, 0, null, null, null, null, null, null, null, null, false);
    X500PrincipalData x500 = data.getSubjectX500Principal();
    assertEquals("CN=Test Subject", x500.getName());
  }

  @Test
  public void testCertificateDataGetIssuerX500Principal() {
    PrincipalData issuerDN = new PrincipalData("CN=Test Issuer");
    CertificateData data = new CertificateData("X.509", new byte[] {1, 2, 3}, 1, null, issuerDN, null, null, null, null,
                                               null, null, null, null, 0, null, null, null, null, null, null, null, null, false);
    X500PrincipalData x500 = data.getIssuerX500Principal();
    assertEquals("CN=Test Issuer", x500.getName());
  }

  @Test
  public void testCertificateDataGetSerialNumber() {
    BigInteger serialNumber = valueOf(12345);
    CertificateData data = new CertificateData("X.509", new byte[] {1, 2, 3}, 1, null, null, serialNumber, null, null, null, null,
                                               null, null, null, 0, null, null, null, null, null, null, null, null, false);
    SerialNumberData serialData = data.getSerialNumberObject();
    assertEquals(serialNumber, serialData.getSerialNumber());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCertificateDataGetExtensionValueNotFound() {
    CertificateData data = new CertificateData("X.509", new byte[] {1, 2, 3}, 1, null, null, null, null, null, null, null, null,
                                               null, null, 0, null, null, null, null, null, new ArrayList<>(), null, null, false);
    data.getExtensionValue("1.2.3.4");
  }

  @Test
  public void testCertificateDataCheckValidity() throws Exception {
    Date notBefore = new Date(currentTimeMillis() - 10000);
    Date notAfter = new Date(currentTimeMillis() + 10000);
    CertificateData data =
        new CertificateData("X.509", new byte[] {1, 2, 3}, 1, null, null, null, notBefore, notAfter, null, null, null, null, null,
                            0, null, null, null, null, null, new ArrayList<>(), null, null, false);
    data.checkValidity();
  }

  @Test(expected = CertificateExpiredException.class)
  public void testCertificateDataCheckValidityExpired() throws Exception {
    Date notBefore = new Date(currentTimeMillis() - 20000);
    Date notAfter = new Date(currentTimeMillis() - 10000);
    CertificateData data =
        new CertificateData("X.509", new byte[] {1, 2, 3}, 1, null, null, null, notBefore, notAfter, null, null, null, null, null,
                            0, null, null, null, null, null, new ArrayList<>(), null, null, false);
    data.checkValidity(new Date(currentTimeMillis()));
  }

  @Test(expected = CertificateNotYetValidException.class)
  public void testCertificateDataCheckValidityNotYetValid() throws Exception {
    Date notBefore = new Date(currentTimeMillis() + 10000);
    Date notAfter = new Date(currentTimeMillis() + 20000);
    CertificateData data =
        new CertificateData("X.509", new byte[] {1, 2, 3}, 1, null, null, null, notBefore, notAfter, null, null, null, null, null,
                            0, null, null, null, null, null, new ArrayList<>(), null, null, false);
    data.checkValidity(new Date(currentTimeMillis()));
  }

  @Test
  public void testCertificateDataFormatSignature() {
    byte[] signature =
        {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13};
    String formatted = CertificateData.formatSignature(signature);

    String expected = "0000: 01 02 03 04 05 06 07 08   09 0A 0B 0C 0D 0E 0F 10  ................\n" +
        "0010: 11 12 13                                          ...\n";
    assertEquals(expected, formatted);
  }

  @Test
  public void testCertificateDataToString() {
    Date notBefore = new Date(currentTimeMillis() - 10000);
    Date notAfter = new Date(currentTimeMillis() + 10000);
    PrincipalData subjectDN = new PrincipalData("CN=Test Subject");
    PrincipalData issuerDN = new PrincipalData("CN=Test Issuer");
    BigInteger serialNumber = new BigInteger("12345678901234567890");
    String algorithm = "RSA";
    byte[] encoded = {1, 2, 3};
    BigInteger modulus = new BigInteger("12345");
    BigInteger params = new BigInteger("67890");
    String publicKeyString = "public key string";
    PublicKeyData publicKeyData = new PublicKeyData(publicKeyString, modulus, params, algorithm, encoded);
    String sigAlgName = "SHA256withRSA";
    String sigAlgOID = "1.2.840.113549.1.1.11";
    byte[] signature = {10, 11, 12};
    String oid = "2.5.29.17";
    boolean criticality = true;
    byte[] value = {1, 2, 3};
    String subjectAlternativeName = "DNS:example.com";
    CertificateExtension extension = new CertificateExtension(oid, criticality, value, subjectAlternativeName);

    List<CertificateExtension> extensions = new ArrayList<>();
    extensions.add(extension);

    CertificateData data =
        new CertificateData("X.509", encoded, 1, subjectDN, issuerDN, serialNumber, notBefore, notAfter,
                            publicKeyData, sigAlgName, sigAlgOID, null, signature,
                            0, null, null, null, null, null, extensions, null, null, false);

    String actualOutput = data.toString();

    assertTrue(actualOutput.contains("Version: V1"));
    assertTrue(actualOutput.contains("Subject: CN=Test Subject"));
    assertTrue(actualOutput.contains("Key:  public key string"));
    assertFalse(data.hasUnsupportedCriticalExtension());

    assertEquals(sigAlgName, data.getSigAlgName());
    assertEquals(sigAlgOID, data.getSigAlgOID());
    assertEquals(signature, data.getSignature());
    assertEquals(notAfter, data.getNotAfter());
    assertEquals(notBefore, data.getNotBefore());

    assertNotNull(data.getExtensionValue(oid));
    assertNull(data.getIssuerUniqueID());
    assertNull(data.getSigAlgParams());
  }
}
