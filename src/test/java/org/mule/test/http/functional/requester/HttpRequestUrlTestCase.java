/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.Method.DELETE;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.runtime.http.api.HttpConstants.Method.POST;
import static org.mule.runtime.http.api.HttpConstants.Method.PUT;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.REQUEST_URL;

import java.util.HashMap;
import java.util.Map;

import io.qameta.allure.Story;
import org.junit.Test;

@Story(REQUEST_URL)
public class HttpRequestUrlTestCase extends AbstractHttpRequestTestCase {

  @Override
  protected String getConfigFile() {
    return "http-request-url-config.xml";
  }

  @Test
  public void simpleRequest() throws Exception {
    flowRunner("simpleRequest").run();

    assertThat(method, is(POST.toString()));
    assertThat(uri, is("/test/test"));
    assertThat(getFirstReceivedHeader("key"), is("value"));
  }

  @Test
  public void expressionRequest() throws Exception {
    flowRunner("expressionRequest").withVariable("url", format("http://localhost:%s/test", httpPort.getNumber())).run();

    assertThat(method, is(PUT.toString()));
    assertThat(uri, is("/test"));
  }

  @Test
  public void uriParamsRequest() throws Exception {
    flowRunner("uriParamsRequest").run();

    assertThat(method, is(DELETE.toString()));
    assertThat(uri, is("/first/second"));
  }

  @Test
  public void queryParamsSimpleRequest() throws Exception {
    flowRunner("queryParamsRequest").withVariable("queryParams", emptyMap()).run();

    assertThat(method, is(GET.toString()));
    assertThat(uri, is("/test?query=param"));
  }

  @Test
  public void queryParamsMergeRequest() throws Exception {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("query1", "param1");
    queryParams.put("query2", "param2");
    flowRunner("queryParamsRequest").withVariable("queryParams", queryParams).run();

    assertThat(method, is(GET.toString()));
    assertThat(uri, is("/test?query=param&query1=param1&query2=param2"));
  }

}
