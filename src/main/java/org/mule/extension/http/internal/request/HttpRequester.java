/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.Integer.getInteger;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.mule.extension.http.api.error.HttpError.CONNECTIVITY;
import static org.mule.extension.http.api.error.HttpError.TIMEOUT;
import static org.mule.extension.http.api.notification.HttpNotificationAction.REQUEST_COMPLETE;
import static org.mule.extension.http.api.notification.HttpNotificationAction.REQUEST_START;
import static org.mule.extension.http.internal.HttpConnectorConstants.IDEMPOTENT_METHODS;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.metadata.TypedValue.of;
import static org.mule.runtime.core.api.config.MuleProperties.SYSTEM_PROPERTY_PREFIX;
import static org.mule.runtime.http.api.HttpConstants.Protocol.HTTPS;
import static reactor.core.publisher.Mono.from;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpError;
import org.mule.extension.http.api.error.HttpErrorMessageGenerator;
import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.extension.http.api.notification.HttpRequestData;
import org.mule.extension.http.api.notification.HttpResponseData;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.authentication.UsernamePasswordAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.internal.HttpStreamingType;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.notification.NotificationEmitter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component capable of performing an HTTP request given a request.
 *
 * @since 1.0
 */
public class HttpRequester {

  private static final Logger logger = LoggerFactory.getLogger(HttpRequester.class);
  public static final String REMOTELY_CLOSED = "Remotely closed";
  public static String RETRY_ATTEMPTS_PROPERTY = SYSTEM_PROPERTY_PREFIX + "http.client.maxRetries";
  public static final int DEFAULT_RETRY_ATTEMPTS = 3;

  private final boolean followRedirects;
  private final HttpRequestAuthentication authentication;
  private final int responseTimeout;
  private final ResponseValidator responseValidator;

  private final HttpRequesterConfig config;
  private final NotificationEmitter notificationEmitter;
  private final HttpRequestFactory eventToHttpRequest;
  private final Scheduler scheduler;
  private final int retryAttempts;

  private HttpErrorMessageGenerator errorMessageGenerator = new HttpErrorMessageGenerator();

  public HttpRequester(HttpRequestFactory eventToHttpRequest, boolean followRedirects, HttpRequestAuthentication authentication,
                       int responseTimeout, ResponseValidator responseValidator, HttpRequesterConfig config,
                       Scheduler scheduler, NotificationEmitter notificationEmitter) {
    this.followRedirects = followRedirects;
    this.authentication = authentication;
    this.responseTimeout = responseTimeout;
    this.responseValidator = responseValidator;
    this.config = config;
    this.scheduler = scheduler;
    this.eventToHttpRequest = eventToHttpRequest;
    this.notificationEmitter = notificationEmitter;
    retryAttempts = getInteger(RETRY_ATTEMPTS_PROPERTY, DEFAULT_RETRY_ATTEMPTS);
  }

  public void doRequest(HttpExtensionClient client, HttpRequesterRequestBuilder requestBuilder,
                        boolean checkRetry, MuleContext muleContext,
                        CompletionCallback<InputStream, HttpResponseAttributes> callback) {
    doRequestWithRetry(client, requestBuilder, checkRetry, muleContext, callback,
                       eventToHttpRequest.create(requestBuilder, authentication), retryAttempts);
  }

  private void doRequestWithRetry(HttpExtensionClient client, HttpRequesterRequestBuilder requestBuilder,
                                  boolean checkRetry, MuleContext muleContext,
                                  CompletionCallback<InputStream, HttpResponseAttributes> callback, HttpRequest httpRequest,
                                  int retryCount) {
    notificationEmitter.fire(REQUEST_START, of(HttpRequestData.from(httpRequest)));
    client.send(httpRequest, responseTimeout, followRedirects, resolveAuthentication(authentication))
        .whenComplete(
                      (response, exception) -> {
                        if (response != null) {
                          notificationEmitter.fire(REQUEST_COMPLETE, of(HttpResponseData.from(response)));
                          HttpResponseToResult httpResponseToResult = new HttpResponseToResult(config, muleContext);
                          from(httpResponseToResult.convert(response, httpRequest.getUri()))
                              .doOnNext(result -> {
                                try {
                                  if (resendRequest(result, checkRetry, authentication)) {
                                    scheduler.submit(() -> consumePayload(result));
                                    doRequest(client, requestBuilder, false, muleContext, callback);
                                  } else {
                                    responseValidator.validate(result, httpRequest);
                                    callback.success(result);
                                  }
                                } catch (Exception e) {
                                  callback.error(e);
                                }
                              })
                              .doOnError(Exception.class, e -> callback.error(e))
                              .subscribe();
                        } else {
                          checkIfRemotelyClosed(exception, client.getDefaultUriParameters());

                          if (shouldRetryRemotelyClosed(exception, retryCount, httpRequest.getMethod())) {
                            doRequestWithRetry(client, requestBuilder, checkRetry, muleContext, callback, httpRequest,
                                               retryCount - 1);
                            return;
                          }

                          logger.error(getErrorMessage(httpRequest));
                          HttpError error = exception instanceof TimeoutException ? TIMEOUT : CONNECTIVITY;
                          callback.error(new HttpRequestFailedException(
                                                                        createStaticMessage(errorMessageGenerator
                                                                            .createFrom(httpRequest, exception.getMessage())),
                                                                        exception, error));
                        }
                      });
  }

  private String getErrorMessage(HttpRequest httpRequest) {
    return String.format("Error sending HTTP request to %s", httpRequest.getUri());
  }

  private boolean resendRequest(Result result, boolean retry, HttpRequestAuthentication authentication) throws MuleException {
    return retry && authentication != null && authentication.shouldRetry(result);
  }

  private void consumePayload(final Result result) {
    if (result.getOutput() instanceof InputStream) {
      try {
        IOUtils.toByteArray((InputStream) result.getOutput());
      } catch (Exception e) {
        throw new MuleRuntimeException(e);
      }
    }
  }

  private HttpAuthentication resolveAuthentication(HttpRequestAuthentication authentication) {
    HttpAuthentication requestAuthentication = null;
    if (authentication instanceof UsernamePasswordAuthentication) {
      requestAuthentication = (HttpAuthentication) authentication;
    }
    return requestAuthentication;
  }

  private void checkIfRemotelyClosed(Throwable exception, UriParameters uriParameters) {
    if (HTTPS.getScheme().equals(uriParameters.getScheme())
        && containsIgnoreCase(exception.getMessage(), REMOTELY_CLOSED)) {
      logger
          .error("Remote host closed connection. Possible SSL/TLS handshake issue. Check protocols, cipher suites and certificate set up. Use -Djavax.net.debug=handshake for further debugging.");
    }
  }

  private boolean shouldRetryRemotelyClosed(Throwable exception, int retryCount, String httpMethod) {
    boolean shouldRetry = IDEMPOTENT_METHODS.contains(httpMethod) && exception instanceof IOException
        && containsIgnoreCase(exception.getMessage(), REMOTELY_CLOSED) && retryCount > 0;
    if (shouldRetry) {
      logger.warn("Sending HTTP message failed with `" + IOException.class.getCanonicalName() + ": " + REMOTELY_CLOSED
          + "`. Request will be retried " + retryCount + " time(s) before failing.");
    }
    return shouldRetry;
  }

  public static class Builder {

    private String uri;
    private String method;
    private boolean followRedirects;
    private HttpStreamingType requestStreamingMode;
    private HttpSendBodyMode sendBodyMode;
    private HttpRequestAuthentication authentication;

    private int responseTimeout;
    private ResponseValidator responseValidator;

    private HttpRequesterConfig config;
    private TransformationService transformationService;
    private Scheduler scheduler;
    private NotificationEmitter notificationEmitter;

    public Builder setUri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder setMethod(String method) {
      this.method = method;
      return this;
    }

    public Builder setFollowRedirects(boolean followRedirects) {
      this.followRedirects = followRedirects;
      return this;
    }

    public Builder setRequestStreamingMode(HttpStreamingType requestStreamingMode) {
      this.requestStreamingMode = requestStreamingMode;
      return this;
    }

    public Builder setSendBodyMode(HttpSendBodyMode sendBodyMode) {
      this.sendBodyMode = sendBodyMode;
      return this;
    }

    public Builder setAuthentication(HttpRequestAuthentication authentication) {
      this.authentication = authentication;
      return this;
    }

    public Builder setResponseTimeout(int responseTimeout) {
      this.responseTimeout = responseTimeout;
      return this;
    }

    public Builder setResponseValidator(ResponseValidator responseValidator) {
      this.responseValidator = responseValidator;
      return this;
    }

    public Builder setConfig(HttpRequesterConfig config) {
      this.config = config;
      return this;
    }

    public Builder setTransformationService(TransformationService transformationService) {
      this.transformationService = transformationService;
      return this;
    }

    public Builder setScheduler(Scheduler scheduler) {
      this.scheduler = scheduler;
      return this;
    }

    public Builder setNotificationEmitter(NotificationEmitter notificationEmitter) {
      this.notificationEmitter = notificationEmitter;
      return this;
    }

    public HttpRequester build() {
      HttpRequestFactory eventToHttpRequest =
          new HttpRequestFactory(config, uri, method, requestStreamingMode, sendBodyMode, transformationService);
      return new HttpRequester(eventToHttpRequest, followRedirects, authentication, responseTimeout,
                               responseValidator, config, scheduler, notificationEmitter);
    }
  }
}
