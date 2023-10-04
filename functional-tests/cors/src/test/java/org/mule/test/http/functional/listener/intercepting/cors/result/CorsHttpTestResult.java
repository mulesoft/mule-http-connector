/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors.result;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.fail;

import org.mule.modules.cors.result.KernelTestResult;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class CorsHttpTestResult implements KernelTestResult {

  private final HttpResponse response;

  public CorsHttpTestResult(HttpResponse response) throws IOException {
    this.response = response;
  }

  @Override
  public Object payload() {
    try {
      HttpEntity entity = response.getEntity();
      return entity != null ? IOUtils.toString(entity.getContent()) : null;
    } catch (IOException e) {
      fail("Could not obtain payload: " + e.getMessage());
      return null;
    }
  }

  @Override
  public List<String> corsHeaders() {
    return headersStream()
        .filter(header -> header
            .getName()
            .toLowerCase()
            .startsWith("access-control"))
        .map(header -> header.getName())
        .collect(toList());
  }

  @Override
  public String header(String headerName) {
    return headersStream()
        .filter(header -> header.getName().equalsIgnoreCase(headerName))
        .map(header -> header.getValue())
        .findFirst()
        .orElse(null);
  }

  @Override
  public int statusCode() {
    return response.getStatusLine().getStatusCode();
  }

  private Stream<Header> headersStream() {
    return newArrayList(response.getAllHeaders()).stream();
  }
}
