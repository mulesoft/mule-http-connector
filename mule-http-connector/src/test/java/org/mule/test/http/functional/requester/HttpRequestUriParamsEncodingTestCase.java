/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.api.HttpMessageBuilder.refreshSystemProperties;
import static org.mule.extension.http.internal.HttpConnectorConstants.ENCODE_URI_PARAMS_PROPERTY;

import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;

public class HttpRequestUriParamsEncodingTestCase extends HttpRequestUriParamsTestCase {

  @Rule
  public SystemProperty uriParamsEncoding = new SystemProperty(ENCODE_URI_PARAMS_PROPERTY, "true");

  @Before
  public void setUp() {
    refreshSystemProperties();
  }

  @AfterClass
  public static void tearDown() {
    refreshSystemProperties();
  }

  @Override
  public void uriParamsContainsReservedUriCharacter() throws Exception {
    flowRunner("reservedUriCharacter")
        .withPayload(TEST_MESSAGE)
        .withVariable("paramName", "testParam")
        .withVariable("paramValue", "$a/word%here\\")
        .run();

    assertThat(uri, equalTo("/testPath/%24a%2Fword%25here%5C"));
  }

  @Override
  public void uriParamsContainsSpaces() throws Exception {
    flowRunner("spaceUriCharacter")
        .withPayload(TEST_MESSAGE)
        .withVariable("param name", "testParam")
        .withVariable("paramValue", "a word+here")
        .run();

    assertThat(uri, equalTo("/testPath/a%20word%2Bhere"));
  }
}
