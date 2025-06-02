/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

// TODO: Move to SDK API
import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.decodeQueryString;
import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.decodeUriParams;

import static java.lang.System.arraycopy;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.extension.http.api.certificate.AlternativeNameData;
import org.mule.extension.http.api.certificate.CertificateData;
import org.mule.extension.http.api.certificate.CertificateExtension;
import org.mule.extension.http.api.certificate.PrincipalData;
import org.mule.extension.http.api.certificate.PublicKeyData;
import org.mule.sdk.api.http.domain.message.request.ClientConnection;
import org.mule.sdk.api.http.domain.message.request.HttpRequest;
import org.mule.sdk.api.http.domain.message.request.HttpRequestContext;

import java.math.BigInteger;
import java.net.URI;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates {@link HttpRequestAttributes} based on an {@link HttpRequestContext}, it's parts and a {@link ListenerPath}.
 */
public class HttpRequestAttributesResolver {

  private static final String QUERY = "?";

  private HttpRequestContext requestContext;
  private ListenerPath listenerPath;

  public HttpRequestAttributesResolver setRequestContext(HttpRequestContext requestContext) {
    this.requestContext = requestContext;
    return this;
  }

  public HttpRequestAttributesResolver setListenerPath(ListenerPath listenerPath) {
    this.listenerPath = listenerPath;
    return this;
  }

  public HttpRequestAttributes resolve() {

    String listenerPath = this.listenerPath.getResolvedPath();
    HttpRequest request = requestContext.getRequest();

    URI uri = request.getUri();
    String path = uri.getPath();
    String rawPath = uri.getRawPath();
    String uriString = path;
    String rawUriString = rawPath;
    String relativePath = this.listenerPath.getRelativePath(path);

    ClientConnection clientConnection = requestContext.getClientConnection();

    String queryString = uri.getQuery();
    String rawQuery = uri.getRawQuery();
    if (queryString != null) {
      uriString += QUERY + queryString;
      rawUriString += QUERY + rawQuery;
    } else {
      queryString = "";
    }

    return new HttpRequestAttributesBuilder()
        .listenerPath(listenerPath)
        .relativePath(relativePath)
        .requestPath(path)
        .rawRequestPath(rawPath)
        .requestUri(uriString)
        .rawRequestUri(rawUriString)
        .method(request.getMethod())
        .scheme(requestContext.getScheme())
        .version(request.getProtocol().asString())
        .headers(request.getHeaders())
        .uriParams(decodeUriParams(listenerPath, rawPath))
        .queryString(queryString)
        .queryParams(decodeQueryString(rawQuery))
        .localAddress(requestContext.getServerConnection().getLocalHostAddress().toString())
        .remoteAddress(clientConnection.getRemoteHostAddress().toString())
        .clientCertificate(() -> {
          try {
            return buildCertificateData(clientConnection);
          } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .build();
  }


  public static CertificateData buildCertificateData(ClientConnection clientConnection) throws Exception {
    Certificate certificate = clientConnection.getClientCertificate();
    if (certificate == null) {
      return null;
    }
    if (!(certificate instanceof X509Certificate)) {
      throw new IllegalArgumentException("Only X509Certificates are supported.");
    }

    X509Certificate x509Certificate = (X509Certificate) certificate;

    String type = x509Certificate.getType();
    byte[] encoded = x509Certificate.getEncoded();
    int version = x509Certificate.getVersion();
    PrincipalData subjectDN = new PrincipalData(x509Certificate.getSubjectDN().getName());
    PrincipalData issuerDN = new PrincipalData(x509Certificate.getIssuerDN().getName());
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

    Set<String> criticalOids = x509Certificate.getCriticalExtensionOIDs();
    Set<String> nonCriticalOids = x509Certificate.getNonCriticalExtensionOIDs();
    boolean hasUnsupportedCriticalExtensions = x509Certificate.hasUnsupportedCriticalExtension();


    return new CertificateData(
                               type, encoded, version, subjectDN, issuerDN, serialNumber, notBefore, notAfter,
                               publicKeyData, sigAlgName, sigAlgOID, sigAlgParams, signature, basicConstraints,
                               extendedKeyUsage, keyUsage, issuerUniqueID, subjectAlternativeNames, issuerAlternativeNames,
                               extensions, criticalOids, nonCriticalOids, hasUnsupportedCriticalExtensions);
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
        arraycopy(extensionValue, 4, value, 0, innerLength);
        return value;
      }
    }
    return extensionValue; // Fallback in case of unexpected format
  }
}
