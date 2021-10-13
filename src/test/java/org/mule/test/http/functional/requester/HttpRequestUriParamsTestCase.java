/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Issue;

public class HttpRequestUriParamsTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Override
  protected String getConfigFile() {
    return "http-request-uri-params-config.xml";
  }

  @Test
  public void sendsUriParamsFromList() throws Exception {
    flowRunner("uriParamList").withPayload(TEST_MESSAGE).withVariable("paramName", "testParam2")
        .withVariable("paramValue", "testValue2").run();
    assertThat(uri, equalTo("/testPath/testValue1/testValue2"));
  }

  @Test
  public void sendsUriParamsFromMap() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("testParam1", "testValue1");
    params.put("testParam2", "testValue2");
    flowRunner("uriParamMap").withPayload(TEST_MESSAGE).withVariable("params", params).run();

    assertThat(uri, equalTo("/testPath/testValue1/testValue2"));
  }

  @Test
  public void overridesUriParams() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("testParam1", "testValueNew");
    params.put("testParam2", "testValue2");
    flowRunner("uriParamOverride").withPayload(TEST_MESSAGE).withVariable("params", params).run();

    assertThat(uri, equalTo("/testPath/testValueNew/testValue2"));
  }

  @Test
  public void sendsUriParamsIfNull() throws Exception {
    expectedException.expectCause(instanceOf(NullPointerException.class));
    expectedException.expectMessage(containsString("Expression {testParam2} evaluated to null."));
    flowRunner("uriParamNull").run();
  }

  @Test
  public void uriParamsContainsReservedUriCharacter() throws Exception {
    flowRunner("reservedUriCharacter")
        .withPayload(TEST_MESSAGE)
        .withVariable("paramName", "testParam")
        .withVariable("paramValue", "$a")
        .run();

    assertThat(uri, equalTo("/testPath/$a"));
  }

  @Test
  @Issue("MULE-18241")
  public void uriParamsContainsSpaces() throws Exception {
    flowRunner("spaceUriCharacter")
        .withPayload(TEST_MESSAGE)
        .withVariable("param name", "testParam")
        .withVariable("paramValue", "a word+here")
        .run();

    assertThat(uri, equalTo("/testPath/a%20word+here"));
  }

  @Test
  public void uriParamsWithRegEx() throws Exception {
    flowRunner("regEx")
        .withPayload(TEST_MESSAGE)
        .withVariable("paramName", "[1-9]")
        .withVariable("paramValue", "abc")
        .run();

    assertThat(uri, equalTo("/testPath/abc"));
  }
}
