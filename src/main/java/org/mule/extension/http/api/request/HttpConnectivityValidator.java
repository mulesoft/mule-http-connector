/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.mule.runtime.api.util.MultiMap.emptyMultiMap;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.DEFAULT_TAB;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.authentication.UsernamePasswordAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.request.HttpRequesterCookieConfig;
import org.mule.extension.http.internal.request.HttpResponseToResult;
import org.mule.extension.http.internal.request.RequestConnectionParams;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;

import java.net.CookieManager;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class HttpConnectivityValidator {

  private static final Logger LOGGER = getLogger(HttpConnectivityValidator.class);

  @Parameter
  @Placement(tab = DEFAULT_TAB, order = 1)
  private String path = "/";

  /**
   * HTTP Method for the request to be sent.
   */
  @Parameter
  @Optional(defaultValue = "GET")
  @Placement(order = 2)
  private String method;

  /**
   * The body of the response message
   */
  @Parameter
  @Content(primary = true)
  @Placement(order = 3)
  private TypedValue<Object> body;

  /**
   * HTTP headers the message should include.
   */
  @Parameter
  @Optional
  @Content
  @NullSafe
  @Placement(order = 4)
  protected MultiMap<String, String> headers = emptyMultiMap();

  /**
   * Query parameters the request should include.
   */
  @Parameter
  @Optional
  @Content
  @DisplayName("Query Parameters")
  @Placement(order = 5)
  protected MultiMap<String, String> queryParameters = emptyMultiMap();

  /**
   * URI parameters that should be used to create the request.
   */
  @Parameter
  @Optional
  @Content
  @DisplayName("URI Parameters")
  @Placement(order = 6)
  protected Map<String, String> uriParameters = emptyMap();

  @Parameter
  @Optional
  @Placement(order = 7)
  private ResponseValidator responseValidator;

  private SuccessStatusCodeValidator defaultStatusCodeValidator = new SuccessStatusCodeValidator("0..399");

  @Override
  public String toString() {
    return "TestConnectionParams{" +
        ", method='" + method + '\'' +
        ", body=" + body +
        ", headers=" + headers +
        ", uriParams=" + uriParameters +
        ", queryParameters=" + queryParameters +
        ", responseValidator=" + responseValidator +
        '}';
  }

  public void validate(HttpExtensionClient client, RequestConnectionParams connectionParams)
      throws ExecutionException, InterruptedException {
    HttpRequest request = buildTestRequest(connectionParams);
    Result<Object, HttpResponseAttributes> result = sendRequest(client, request);
    validateResult(request, (Result) result);
  }

  private void validateResult(HttpRequest request, Result result) {
    getResponseValidator().validate(result, request);
  }

  private Result<Object, HttpResponseAttributes> sendRequest(HttpExtensionClient client, HttpRequest request)
      throws InterruptedException, ExecutionException {
    HttpResponse response =
        client.send(request, 999999, false, resolveAuthentication(client)).get();

    Result<Object, HttpResponseAttributes> result = new HttpResponseToResult()
        .convert(new VoidHttpRequesterCookieConfig(), null, response, response.getEntity(), response.getEntity()::getContent,
                 request.getUri());
    return result;
  }

  private HttpRequest buildTestRequest(RequestConnectionParams connectionParams) {
    HttpRequesterRequestBuilder requestBuilder = new HttpRequesterRequestBuilder();
    requestBuilder.setUriParams(uriParameters);

    String uriString = getUriString(connectionParams);
    return HttpRequest.builder()
        .uri(uriString)
        .method(method)
        .headers(headers)
        .queryParams(queryParameters)
        .build();
  }

  private String getUriString(RequestConnectionParams connectionParams) {
    return format("%s://%s:%s%s", connectionParams.getProtocol().getScheme(), connectionParams.getHost(),
                  connectionParams.getPort(), path);
  }

  private ResponseValidator getResponseValidator() {
    return responseValidator == null ? defaultStatusCodeValidator : responseValidator;
  }

  private static class VoidHttpRequesterCookieConfig implements HttpRequesterCookieConfig {

    @Override
    public boolean isEnableCookies() {
      return false;
    }

    @Override
    public CookieManager getCookieManager() {
      return null;
    }
  }

  private static HttpAuthentication resolveAuthentication(HttpExtensionClient client) {
    HttpRequestAuthentication authentication = client.getDefaultAuthentication();
    if (authentication instanceof UsernamePasswordAuthentication) {
      return (HttpAuthentication) authentication;
    } else {
      return null;
    }
  }

  public void setHeaders(MultiMap<String, String> headers) {
    this.headers = headers;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setResponseValidator(ResponseValidator responseValidator) {
    this.responseValidator = responseValidator;
  }

  public void setUriParameters(Map<String, String> uriParameters) {
    this.uriParameters = uriParameters;
  }

  public void setBody(TypedValue<Object> body) {
    this.body = body;
  }
}
