/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener;

import static java.lang.Boolean.getBoolean;
import static java.util.Collections.emptyMap;
import static org.mule.extension.http.api.HttpHeaders.Names.AUTHORIZATION;
import static org.mule.extension.http.api.HttpHeaders.Names.WWW_AUTHENTICATE;
import static org.mule.extension.http.internal.HttpConnectorConstants.BASIC_LAX_DECODING_PROPERTY;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.meta.ExpressionSupport.REQUIRED;
import static org.mule.runtime.core.api.config.i18n.CoreMessages.authFailedForUser;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.UNAUTHORIZED;

import org.mule.extension.http.api.HttpListenerResponseAttributes;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.internal.filter.BasicUnauthorisedException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.security.Authentication;
import org.mule.runtime.api.security.Credentials;
import org.mule.runtime.api.security.SecurityException;
import org.mule.runtime.api.security.SecurityProviderNotFoundException;
import org.mule.runtime.api.security.UnauthorisedException;
import org.mule.runtime.api.security.UnknownAuthenticationTypeException;
import org.mule.runtime.api.security.UnsupportedAuthenticationSchemeException;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.security.AuthenticationHandler;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter for basic authentication over an HTTP request.
 *
 * @since 1.0
 */
public class HttpBasicAuthenticationFilter {

  private static final String HEADER_AUTHORIZATION = AUTHORIZATION.toLowerCase();
  private static final char PADDING = '=';
  private static final Decoder DECODER = Base64.getDecoder();
  private static boolean LAX_DECODING = getBoolean(BASIC_LAX_DECODING_PROPERTY);

  protected static final Logger logger = LoggerFactory.getLogger(HttpBasicAuthenticationFilter.class);

  /**
   * Authentication realm.
   */
  @Parameter
  private String realm;

  /**
   * The delegate-security-provider to use for authenticating. Use this in case you have multiple security managers defined in
   * your configuration.
   */
  @Parameter
  @Optional
  @NullSafe
  private List<String> securityProviders;

  /**
   * The {@link HttpRequestAttributes} coming from an HTTP listener source to check the 'Authorization' header.
   */
  @Parameter
  @Optional(defaultValue = "#[attributes]")
  @Expression(REQUIRED)
  HttpRequestAttributes attributes;

  /**
   * Authenticates an HTTP message based on the provided {@link HttpRequestAttributes}.
   *
   * @throws SecurityException if authentication fails
   */
  public void authenticate(AuthenticationHandler authenticationHandler)
      throws SecurityException, SecurityProviderNotFoundException, UnknownAuthenticationTypeException {
    String header = attributes.getHeaders().get(HEADER_AUTHORIZATION);

    if (logger.isDebugEnabled()) {
      logger.debug("Authorization header: " + header);
    }

    if ((header != null) && header.startsWith("Basic ")) {
      String base64Token = header.substring(6);
      if (LAX_DECODING) {
        // commons-codec ignored the characters beyond the padding
        base64Token = base64Token.substring(0, base64Token.lastIndexOf(PADDING) + 1);
      }
      String token;
      try {
        token = new String(DECODER.decode(base64Token.getBytes()));
      } catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Authentication request failed: " + e.toString());
        }
        throw new BasicUnauthorisedException(createStaticMessage("Could not decode authorization header."), e,
                                             createUnauthenticatedMessage());
      }

      String username = "";
      String password = "";
      int delim = token.indexOf(":");

      if (delim != -1) {
        username = token.substring(0, delim);
        password = token.substring(delim + 1);
      }

      Credentials credentials = authenticationHandler.createCredentialsBuilder()
          .withUsername(username)
          .withPassword(password.toCharArray())
          .build();

      try {
        authenticationHandler
            .setAuthentication(securityProviders,
                               authenticationHandler.createAuthentication(credentials)
                                   .setProperties(authenticationProperties(authenticationHandler)));
      } catch (UnauthorisedException e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Authentication request for user: " + username + " failed: " + e.toString());
        }
        throw new BasicUnauthorisedException(authFailedForUser(username), e, createUnauthenticatedMessage());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Authentication success.");
      }

    } else if (header == null) {
      throw new BasicUnauthorisedException(null, "HTTP basic authentication", "HTTP listener", createUnauthenticatedMessage());
    } else {
      throw new UnsupportedAuthenticationSchemeException(createStaticMessage("Http Basic filter doesn't know how to handle header "
          + header), createUnauthenticatedMessage());
    }
  }

  private Map<String, Object> authenticationProperties(AuthenticationHandler authenticationHandler) {
    return authenticationHandler.getAuthentication().map(Authentication::getProperties).orElse(emptyMap());
  }

  private Message createUnauthenticatedMessage() {
    String realmHeader = "Basic realm=";
    if (realm != null) {
      realmHeader += "\"" + realm + "\"";
    }
    CaseInsensitiveMultiMap headers = new CaseInsensitiveMultiMap();
    headers.put(WWW_AUTHENTICATE, realmHeader);
    return Message.builder().nullValue().attributesValue(new HttpListenerResponseAttributes(UNAUTHORIZED.getStatusCode(),
                                                                                            UNAUTHORIZED.getReasonPhrase(),
                                                                                            headers))
        .build();
  }

  public static void refreshSystemProperties() {
    LAX_DECODING = getBoolean(BASIC_LAX_DECODING_PROPERTY);
  }

}
