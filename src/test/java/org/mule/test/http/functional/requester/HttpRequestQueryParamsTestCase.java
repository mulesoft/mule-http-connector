/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class HttpRequestQueryParamsTestCase extends AbstractHttpRequestTestCase {

  @Override
  protected String getConfigFile() {
    return "http-request-query-params-config.xml";
  }

  @Test
  public void sendsQueryParamsFromList() throws Exception {
    flowRunner("queryParamList").withPayload(TEST_MESSAGE).withVariable("paramName", "testName2")
        .withVariable("paramValue", "testValue2").run();

    assertThat(uri, equalTo("/testPath?testName1=testValue1&testName2=testValue2"));
  }

  @Test
  public void sendsQueryParamsFromMap() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("testName1", "testValue1");
    params.put("testName2", "testValue2");
    flowRunner("queryParamMap").withPayload(TEST_MESSAGE).withVariable("params", params).run();
    assertThat(uri, equalTo("/testPath?testName1=testValue1&testName2=testValue2"));
  }

  @Test
  public void queryParamsOverride() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("testName1", "testValueNew");
    params.put("testName2", "testValue2");
    flowRunner("multipleQueryParam").withPayload(TEST_MESSAGE).withVariable("params", params).run();

    assertThat(uri, equalTo("/testPath?testName1=testValueNew&testName1=testValue1&testName2=testValue2"));
  }

  @Test
  public void sendsQueryParamsNulls() throws Exception {
    flowRunner("queryParamNulls").withPayload(TEST_MESSAGE).run();
    assertThat(uri, equalTo("/testPath?testName1&testName2"));
  }

  @Test
  public void queryParamDefaultsOnly() throws Exception {
    flowRunner("queryParamDefaultsOnly").withPayload(TEST_MESSAGE).run();
    assertThat(uri, equalTo("/testPath?filter=username%2Capps"));
  }

  @Test
  public void queryParamAppendDefault() throws Exception {
    flowRunner("queryParamAppendDefault").withPayload(TEST_MESSAGE).run();
    assertThat(uri, equalTo("/testPath?testName1=testValue1&filter=username%2Capps"));
  }

  @Test
  public void queryParamsDuplicatedDefaults() throws Exception {
    flowRunner("queryParamsDuplicatedDefaults").withPayload(TEST_MESSAGE).run();
    assertThat(uri, equalTo("/testPath?filter=username&filter=apps"));
  }

  @Test
  public void queryParamMultiKeyDefault() throws Exception {
    flowRunner("queryParamMultiKeyDefault").withPayload(TEST_MESSAGE).run();
    assertThat(uri, equalTo("/testPath?filter=level&filter=username%2Capps"));
  }

}
