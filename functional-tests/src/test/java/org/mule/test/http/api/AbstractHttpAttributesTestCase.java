/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.api;

import org.mule.runtime.api.util.MultiMap;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.HashMap;
import java.util.Map;

public class AbstractHttpAttributesTestCase extends AbstractMuleTestCase {

  protected MultiMap<String, String> getHeaders() {
    MultiMap headers = new MultiMap();
    headers.put("header1", "headerValue1");
    headers.put("header2", "headerValue2");
    return headers;
  }

  protected MultiMap<String, String> getQueryParams() {
    MultiMap queryParams = new MultiMap();
    queryParams.put("queryParam1", "queryParamValue1");
    queryParams.put("queryParam2", "queryParamValue2");
    return queryParams;
  }

  protected Map<String, String> getUriParams() {
    Map uriParams = new HashMap();
    uriParams.put("uriParam1", "uriParamValue1");
    uriParams.put("uriParam2", "uriParamValue2");
    return uriParams;
  }

  protected <T extends Map<String, String>> T prepareSensitiveDataMap(T params) {
    params.put("password", "4n3zP4SSW0rd");
    params.put("pass", "s0m3P4zz");
    params.put("client_secret", "myPr3c10us");
    params.put("authorization", "1234");
    params.put("regular", "show me");
    return params;
  }

}
