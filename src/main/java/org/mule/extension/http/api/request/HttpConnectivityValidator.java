/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.extension.http.internal.request.UriUtils.replaceUriParams;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.authentication.UsernamePasswordAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterTestRequestBuilder;
import org.mule.extension.http.api.request.builder.KeyValuePair;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.ResponseValidatorTypedException;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.request.HttpRequesterCookieConfig;
import org.mule.extension.http.internal.request.HttpRequesterProvider;
import org.mule.extension.http.internal.request.HttpResponseToResult;
import org.mule.extension.http.internal.request.RequestConnectionParams;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.net.CookieManager;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Requester connectivity validator. It validates the connections created by the {@link HttpRequesterProvider}.
 *
 * @since 1.7
 */
public class HttpConnectivityValidator implements Initialisable, Disposable {

  private static final Logger LOGGER = getLogger(HttpConnectivityValidator.class);

  /**
   * Path used in the connectivity test request URI.
   */
  @Parameter
  @DisplayName("Test Request Path")
  @Placement(order = 1)
  @Expression(NOT_SUPPORTED)
  private String requestPath = "/";

  /**
   * HTTP Method for the connectivity test request.
   */
  @Parameter
  @DisplayName("Request HTTP Method")
  @Optional(defaultValue = "GET")
  @Placement(order = 2)
  @Expression(NOT_SUPPORTED)
  private String requestMethod = "GET";

  /**
   * Specifies whether to follow redirects or not.
   */
  @Parameter
  @Optional(defaultValue = "false")
  @Placement(order = 3)
  private boolean followRedirects = false;

  /**
   * Maximum time that the request element will block the execution of the flow waiting for the HTTP response.
   */
  @Parameter
  @Optional(defaultValue = "10000")
  @Placement(order = 4)
  private Integer responseTimeout = 10000;

  /**
   * Response timeout time unit.
   */
  @Parameter
  @Optional(defaultValue = "MILLISECONDS")
  @Placement(order = 5)
  private TimeUnit responseTimeoutUnit = MILLISECONDS;

  /**
   * Request builder to be used to connectivity test.
   */
  @ParameterGroup(name = "Request Builder", showInDsl = false)
  @Placement(order = 6)
  private HttpRequesterTestRequestBuilder requestBuilder;

  /**
   * Validation applied to the connectivity test response.
   */
  @Parameter
  @Optional
  @DisplayName("Response Validator")
  @Placement(order = 7)
  @Expression(NOT_SUPPORTED)
  private ResponseValidator responseValidator;

  private SuccessStatusCodeValidator defaultStatusCodeValidator = new SuccessStatusCodeValidator("0..399");

  @Inject
  // It's only used to propagate the initialisation to response validator.
  private MuleContext muleContext;

  public void validate(HttpExtensionClient client, RequestConnectionParams connectionParams)
      throws ExecutionException, InterruptedException, ResponseValidatorTypedException {
    HttpRequest request = buildTestRequest(connectionParams);
    Result<Object, HttpResponseAttributes> result = sendRequest(client, request);
    validateResult(request, result);
  }

  private void validateResult(HttpRequest request, Result result) {
    getResponseValidator().validate(result, request);
  }

  private Result<Object, HttpResponseAttributes> sendRequest(HttpExtensionClient client, HttpRequest request)
      throws InterruptedException, ExecutionException {
    int responseTimeoutInt = (int) responseTimeoutUnit.toMillis(responseTimeout.longValue());
    HttpResponse response =
        client.send(request, responseTimeoutInt, followRedirects, resolveAuthentication(client)).get();

    return new HttpResponseToResult()
        .convert(new VoidHttpRequesterCookieConfig(), muleContext, response, response.getEntity(),
                 response.getEntity()::getContent,
                 request.getUri());
  }

  private HttpRequest buildTestRequest(RequestConnectionParams connectionParams) {
    String uriString = getUriString(connectionParams);
    return HttpRequest.builder()
        .uri(uriString)
        .method(requestMethod)
        .headers(toMultiMap(requestBuilder.getRequestHeaders()))
        .queryParams(toMultiMap(requestBuilder.getRequestQueryParams()))
        .entity(requestBuilder.buildEntity())
        .build();
  }

  private static MultiMap<String, String> toMultiMap(Iterable<? extends KeyValuePair> asList) {
    MultiMap<String, String> asMultiMap = new MultiMap<>();
    asList.forEach(pair -> asMultiMap.put(pair.getKey(), pair.getValue()));
    return asMultiMap;
  }

  private String getUriString(RequestConnectionParams connectionParams) {
    String pathWithUriParams = replaceUriParams(requestPath, requestBuilder.getRequestUriParams());
    return format("%s://%s:%s%s", connectionParams.getProtocol().getScheme(), connectionParams.getHost(),
                  connectionParams.getPort(), pathWithUriParams);
  }

  private ResponseValidator getResponseValidator() {
    return responseValidator == null ? defaultStatusCodeValidator : responseValidator;
  }

  @Override
  public void dispose() {
    // Added just for symmetry with initialise.
  }

  @Override
  public void initialise() throws InitialisationException {
    if (responseValidator != null) {
      initialiseIfNeeded(responseValidator, true, muleContext);
    }
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
}
