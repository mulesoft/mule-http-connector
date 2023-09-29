/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.policy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.api.policy.HttpPolicyRequestAttributes;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.util.List;

import com.google.common.collect.Lists;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;

@Story("Multi-values preservation in policy")
public class HttpRequestPolicyAttributesTestCase extends AbstractHttpTestCase {

  private static final String HEADER = "header";
  private static final String QUERY_PARAM = "queryParam";
  private static final List<String> VALUES = Lists.newArrayList("first", "second");

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Override
  protected String getConfigFile() {
    return "http-request-policy-attributes-config.xml";
  }

  @Test
  @Description("When setting HttpPolicyRequestAttributes through DW, pre-existent multi-value headers are preserved")
  public void multiValueHeadersArePreserved() throws Exception {
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put(HEADER, VALUES);

    CoreEvent response = flowRunner("multi-value-headers").withVariable("headers", headers).run();

    HttpPolicyRequestAttributes attributes = (HttpPolicyRequestAttributes) response.getMessage().getPayload().getValue();
    assertThat(attributes.getHeaders().getAll(HEADER), is(VALUES));
  }

  @Test
  @Description("When setting HttpPolicyRequestAttributes through DW, pre-existent multi-value query params are preserved")
  public void multiValueQueryParamsArePreserved() throws Exception {
    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put(QUERY_PARAM, VALUES);

    CoreEvent response = flowRunner("multi-value-query-params").withVariable("queryParams", queryParams).run();

    HttpPolicyRequestAttributes attributes = (HttpPolicyRequestAttributes) response.getMessage().getPayload().getValue();
    assertThat(attributes.getQueryParams().getAll(QUERY_PARAM), is(VALUES));
  }
}
