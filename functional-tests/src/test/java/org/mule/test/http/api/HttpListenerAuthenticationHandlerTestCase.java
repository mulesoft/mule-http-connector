/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.extension.http.api.HttpHeaders.Names.AUTHORIZATION;

import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mule.apache.xerces.impl.dv.util.Base64;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.listener.HttpBasicAuthenticationFilter;
import org.mule.extension.http.internal.filter.BasicUnauthorisedException;
import org.mule.runtime.api.security.Authentication;
import org.mule.runtime.api.security.CredentialsBuilder;
import org.mule.runtime.api.security.DefaultMuleAuthentication;
import org.mule.runtime.api.security.UnauthorisedException;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.security.DefaultMuleCredentials;
import org.mule.runtime.extension.api.security.AuthenticationHandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class HttpListenerAuthenticationHandlerTestCase extends AbstractHttpAttributesTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Rule
  public ExpectedException expected = none();

  @Captor
  private ArgumentCaptor<Authentication> captor;

  @Captor
  private ArgumentCaptor<char[]> passwordCaptor;

  @Mock
  private AuthenticationHandler authenticationHandler;

  @Mock
  private CredentialsBuilder credentialsBuilder;
  private Authentication authentication;
  private HttpRequestAttributes attributes;
  private MultiMap<String, String> headers;
  private List<String> securityProviders;

  private static final String HEADER_AUTHORIZATION = AUTHORIZATION.toLowerCase();

  @Before
  public void setUp() {
    authentication = new DefaultMuleAuthentication(new DefaultMuleCredentials("mockedUsername", "mockedPassword".toCharArray()))
        .setProperties(of("thisIsAProperty", "thisIsAValue"));
    when(authenticationHandler.getAuthentication()).thenReturn(Optional.of(authentication));
    when(credentialsBuilder.withPassword(any())).thenReturn(credentialsBuilder);
    when(credentialsBuilder.withUsername(any())).thenReturn(credentialsBuilder);
    DefaultMuleCredentials newCredentials = new DefaultMuleCredentials("user", "password".toCharArray());
    when(credentialsBuilder.build()).thenReturn(newCredentials);
    when(authenticationHandler.createCredentialsBuilder()).thenReturn(credentialsBuilder);
    when(authenticationHandler.createAuthentication(newCredentials)).thenReturn(new DefaultMuleAuthentication(newCredentials));

    securityProviders = new ArrayList<>();
    attributes = mock(HttpRequestAttributes.class);
    headers = new MultiMap<String, String>() {

      {
        put(HEADER_AUTHORIZATION, "Basic " + new String(Base64.encode("user:password".getBytes()).getBytes()));
      }
    };
    when(attributes.getHeaders()).thenReturn(headers);
  }

  @Test
  public void propertiesFromPreviousAuthenticationArePreserved() throws Exception {
    HttpBasicAuthenticationFilter httpBasicAuthenticationFilter = new HttpBasicAuthenticationFilter();

    setMocks(httpBasicAuthenticationFilter, "attributes", attributes);
    setMocks(httpBasicAuthenticationFilter, "securityProviders", securityProviders);

    httpBasicAuthenticationFilter.authenticate(authenticationHandler);

    verify(authenticationHandler).setAuthentication(eq(securityProviders), captor.capture());

    Authentication authentication = captor.getValue();
    assertThat(authentication, notNullValue());
    assertThat(authentication.getProperties(), hasEntry("thisIsAProperty", "thisIsAValue"));
    assertThat(authentication.getPrincipal(), is("user"));
    assertThat(authentication.getCredentials(), is("password"));
  }

  @Test
  public void passwordIsCorrectlyParsedByDecodeToken() throws Exception {
    DefaultMuleCredentials newCredentials = new DefaultMuleCredentials("mule12345", "mule12345§".toCharArray());
    when(credentialsBuilder.build()).thenReturn(newCredentials);
    when(authenticationHandler.createCredentialsBuilder()).thenReturn(credentialsBuilder);
    when(authenticationHandler.createAuthentication(newCredentials)).thenReturn(new DefaultMuleAuthentication(newCredentials));
    headers = new MultiMap<String, String>() {

      {
        put(HEADER_AUTHORIZATION, "Basic " + new String(Base64.encode("mule12345:mule12345§".getBytes()).getBytes()));
      }
    };
    when(attributes.getHeaders()).thenReturn(headers);

    HttpBasicAuthenticationFilter httpBasicAuthenticationFilter = new HttpBasicAuthenticationFilter();

    setMocks(httpBasicAuthenticationFilter, "attributes", attributes);
    setMocks(httpBasicAuthenticationFilter, "securityProviders", securityProviders);

    httpBasicAuthenticationFilter.authenticate(authenticationHandler);

    verify(authenticationHandler).setAuthentication(eq(securityProviders), captor.capture());
    verify(this.credentialsBuilder).withPassword(passwordCaptor.capture());

    Authentication authentication = captor.getValue();
    char[] decodedPassword = passwordCaptor.getValue();
    assertThat(authentication, notNullValue());
    assertThat(authentication.getProperties(), hasEntry("thisIsAProperty", "thisIsAValue"));
    assertThat(authentication.getPrincipal(), is("mule12345"));
    assertThat(authentication.getCredentials(), is("mule12345§"));
    assertThat(decodedPassword, is("mule12345§".toCharArray()));
  }

  @Test
  public void unauthorisedExceptionProperlyMapped() throws Exception {
    HttpBasicAuthenticationFilter httpBasicAuthenticationFilter = new HttpBasicAuthenticationFilter();

    setMocks(httpBasicAuthenticationFilter, "attributes", attributes);
    setMocks(httpBasicAuthenticationFilter, "securityProviders", securityProviders);

    doThrow(UnauthorisedException.class).when(authenticationHandler).setAuthentication(any(), any());
    expected.expect(BasicUnauthorisedException.class);
    expected.expectMessage(containsString("Authentication failed for principal user"));

    httpBasicAuthenticationFilter.authenticate(authenticationHandler);
  }

  private void setMocks(HttpBasicAuthenticationFilter httpBasicAuthenticationFilter, String fieldName, Object fieldMock)
      throws NoSuchFieldException, IllegalAccessException {
    Field attributes = HttpBasicAuthenticationFilter.class.getDeclaredField(fieldName);
    boolean attributesAccessible = attributes.isAccessible();
    attributes.setAccessible(true);
    attributes.set(httpBasicAuthenticationFilter, fieldMock);
    attributes.setAccessible(attributesAccessible);
  }

}
