/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mule.runtime.core.api.config.i18n.CoreMessages.cannotLoadFromClasspath;
import static org.mule.runtime.core.api.util.IOUtils.getResourceAsStream;

import org.mule.extension.http.AlternativeNameData;
import org.mule.extension.http.CertificateData;
import org.mule.extension.http.CertificateExtension;
import org.mule.extension.http.PrincipalData;
import org.mule.extension.http.PublicKeyData;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.qameta.allure.Description;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpRequestAttributesSerializationTestCase extends AbstractHttpAttributesTestCase {

  private static Certificate certificate;

  private HttpRequestAttributesBuilder baseBuilder =
      new HttpRequestAttributesBuilder()
          .listenerPath("/listener/path")
          .relativePath("/relative/path")
          .version("1.0")
          .scheme("scheme")
          .method("GET")
          .requestPath("/request/path")
          .remoteAddress("http://10.1.2.5:8080/")
          .localAddress("http://127.0.0.1:8080/")
          .requestUri("http://127.0.0.1/gateway")
          .headers(getHeaders())
          .queryString("queryParam1=queryParam1&queryParam2=queryParam2")
          .queryParams(getQueryParams())
          .uriParams(getUriParams());

  @BeforeClass
  public static void setup() throws Exception {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

    InputStream is = getResourceAsStream("tls/serverKeystore", HttpRequestAttributesSerializationTestCase.class);
    if (null == is) {
      throw new FileNotFoundException(cannotLoadFromClasspath("serverKeystore").getMessage());
    }

    keyStore.load(is, "mulepassword".toCharArray());
    certificate = keyStore.getCertificate("muleserver");
  }

  @Test
  @Description("HttpRequestAttributes are correctly serialized and deserialized even if no certificate was defined")
  public void withNoCertificate() {
    HttpRequestAttributes processed = assertSerialization(baseBuilder.build());
    assertThat(processed.getClientCertificate(), is(nullValue()));
  }

  @Test
  @Description("HttpRequestAttributes are correctly serialized and deserialized with an explicit certificate. Certificate can be recover after deserialization")
  public void withResolvedCertificate() throws Exception {
    HttpRequestAttributes processed =
        assertSerialization(baseBuilder.clientCertificate(buildCertificateData(certificate)).build());
    assertThat(processed.getClientCertificate().toString(), is(certificate.toString()));
  }

  @Test
  @Description("HttpRequestAttributes are correctly serialized and deserialized with a certificate supplier. Certificate can be recover after deserialization")
  public void withLazyCertificate() {
    HttpRequestAttributes processed = assertSerialization(baseBuilder.clientCertificate(() -> {
      try {
        return buildCertificateData(certificate);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).build());
    assertThat(processed.getClientCertificate().toString(), is(certificate.toString()));
  }

  private HttpRequestAttributes assertSerialization(HttpRequestAttributes original) {
    HttpRequestAttributes processed = deserialize(serialize(original));
    assertThat(processed.getListenerPath(), equalTo(original.getListenerPath()));
    assertThat(processed.getRelativePath(), equalTo(original.getRelativePath()));
    assertThat(processed.getVersion(), equalTo(original.getVersion()));
    assertThat(processed.getScheme(), equalTo(original.getScheme()));
    assertThat(processed.getMethod(), equalTo(original.getMethod()));
    assertThat(processed.getRequestPath(), equalTo(original.getRequestPath()));
    assertThat(processed.getRemoteAddress(), equalTo(original.getRemoteAddress()));
    assertThat(processed.getLocalAddress(), equalTo(original.getLocalAddress()));
    assertThat(processed.getRequestUri(), equalTo(original.getRequestUri()));
    assertThat(processed.getHeaders(), equalTo(original.getHeaders()));
    assertThat(processed.getQueryString(), equalTo(original.getQueryString()));
    assertThat(processed.getQueryParams(), equalTo(original.getQueryParams()));
    assertThat(processed.getUriParams(), equalTo(original.getUriParams()));
    assertThat(processed.getMaskedRequestPath(), equalTo(original.getMaskedRequestPath()));

    return processed;
  }

  public static CertificateData buildCertificateData(Certificate certificate) throws Exception {
    if (!(certificate instanceof X509Certificate)) {
      throw new IllegalArgumentException("Only X509Certificates are supported.");
    }

    X509Certificate x509Certificate = (X509Certificate) certificate;

    String type = x509Certificate.getType();
    byte[] encoded = x509Certificate.getEncoded();
    int version = x509Certificate.getVersion();
    PrincipalData subjectDN = new PrincipalData(x509Certificate.getSubjectX500Principal().getName());
    PrincipalData issuerDN = new PrincipalData(x509Certificate.getIssuerX500Principal().getName());
    BigInteger serialNumber = x509Certificate.getSerialNumber();
    Date notBefore = x509Certificate.getNotBefore();
    Date notAfter = x509Certificate.getNotAfter();
    // Get the public key
    RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
    // Extract the key details
    BigInteger modulus = publicKey.getModulus();
    BigInteger publicExponent = publicKey.getPublicExponent();
    PublicKeyData publicKeyData =
        new PublicKeyData(publicKey.toString(), modulus, publicExponent, publicKey.getAlgorithm(), publicKey.getEncoded());
    String sigAlgName = x509Certificate.getSigAlgName();
    String sigAlgOID = x509Certificate.getSigAlgOID();
    byte[] sigAlgParams = x509Certificate.getSigAlgParams();
    byte[] signature = x509Certificate.getSignature();
    int basicConstraints = x509Certificate.getBasicConstraints();
    List<String> extendedKeyUsage = Collections.emptyList();
    try {
      extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
    } catch (Exception e) {
      // Ignoring exception
    }
    boolean[] keyUsage = x509Certificate.getKeyUsage();
    boolean[] issuerUniqueID = x509Certificate.getIssuerUniqueID();
    List<AlternativeNameData> subjectAlternativeNames = Collections.emptyList();
    try {
      Collection<List<?>> sans = x509Certificate.getSubjectAlternativeNames();
      if (sans != null) {
        subjectAlternativeNames = sans.stream()
            .map(san -> new AlternativeNameData(((Number) san.get(0)).intValue(), san.get(1).toString()))
            .collect(Collectors.toList());
      }
    } catch (Exception e) {
      // Ignoring exception
    }
    List<AlternativeNameData> issuerAlternativeNames = Collections.emptyList();
    try {
      Collection<List<?>> ians = x509Certificate.getIssuerAlternativeNames();
      if (ians != null) {
        issuerAlternativeNames = ians.stream()
            .map(ian -> new AlternativeNameData(((Number) ian.get(0)).intValue(), ian.get(1).toString()))
            .collect(Collectors.toList());
      }
    } catch (Exception e) {
      // Ignoring exception
    }
    // Populate extensions list
    List<CertificateExtension> extensions = new ArrayList<>();

    for (String oid : x509Certificate.getNonCriticalExtensionOIDs()) {
      extensions.add(new CertificateExtension(oid, false, decodeExtensionValue(x509Certificate.getExtensionValue(oid)),
                                              parseSubjectAlternativeName(x509Certificate)));
    }

    for (String oid : x509Certificate.getCriticalExtensionOIDs()) {
      extensions.add(new CertificateExtension(oid, true, decodeExtensionValue(x509Certificate.getExtensionValue(oid)),
                                              parseSubjectAlternativeName(x509Certificate)));
    }
    Collections.reverse(extensions);


    return new CertificateData(
                               type, encoded, version, subjectDN, issuerDN, serialNumber, notBefore, notAfter,
                               publicKeyData, sigAlgName, sigAlgOID, sigAlgParams, signature, basicConstraints,
                               extendedKeyUsage, keyUsage, issuerUniqueID, subjectAlternativeNames, issuerAlternativeNames,
                               extensions);
  }

  public static String parseSubjectAlternativeName(X509Certificate certificate) {
    StringBuilder sb = new StringBuilder();
    try {
      Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
      if (subjectAlternativeNames != null) {
        for (List<?> san : subjectAlternativeNames) {
          Integer type = (Integer) san.get(0);
          String value = san.get(1).toString();
          switch (type) {
            case 2:
              sb.append("  DNSName: ").append(value).append("\n");
              break;
            case 7:
              sb.append("  IPAddress: ").append(value).append("\n");
              break;
            default:
              sb.append("  OtherName: ").append(value).append("\n");
              break;
          }
        }
      }
    } catch (CertificateParsingException e) {
      sb.append("  Error parsing Subject Alternative Name: ").append(e.getMessage()).append("\n");
    }
    return sb.toString();
  }

  private static byte[] decodeExtensionValue(byte[] extensionValue) throws Exception {
    // The extension value is wrapped in an OCTET STRING, so we need to decode it
    if (extensionValue[0] == 0x04) {
      // The actual value starts from index 4 after the OCTET STRING header
      // ASN.1 DER encoded bytes: 0x04 (OCTET STRING tag) | length | 0x04 (inner OCTET STRING tag) | length | actual value
      int length = extensionValue[1] & 0xFF;
      if (length == (extensionValue.length - 2)) {
        // Inner OCTET STRING
        int innerLength = extensionValue[3] & 0xFF;
        byte[] value = new byte[innerLength];
        System.arraycopy(extensionValue, 4, value, 0, innerLength);
        return value;
      }
    }
    return extensionValue; // Fallback in case of unexpected format
  }

}
