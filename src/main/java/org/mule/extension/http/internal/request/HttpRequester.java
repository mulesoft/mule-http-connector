/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.getInteger;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.mule.extension.http.api.error.HttpError.CONNECTIVITY;
import static org.mule.extension.http.api.error.HttpError.TIMEOUT;
import static org.mule.extension.http.api.notification.HttpNotificationAction.REQUEST_COMPLETE;
import static org.mule.extension.http.api.notification.HttpNotificationAction.REQUEST_START;
import static org.mule.extension.http.internal.HttpConnectorConstants.DEFAULT_RETRY_ATTEMPTS;
import static org.mule.extension.http.internal.HttpConnectorConstants.IDEMPOTENT_METHODS;
import static org.mule.extension.http.internal.HttpConnectorConstants.REMOTELY_CLOSED;
import static org.mule.extension.http.internal.HttpConnectorConstants.RETRY_ATTEMPTS_PROPERTY;
import static org.mule.extension.http.internal.HttpConnectorConstants.RETRY_ON_ALL_METHODS_PROPERTY;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.http.api.HttpConstants.Protocol.HTTPS;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.error.HttpError;
import org.mule.extension.http.api.error.HttpErrorMessageGenerator;
import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.extension.http.api.notification.HttpRequestNotificationData;
import org.mule.extension.http.api.notification.HttpResponseNotificationData;
import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.authentication.UsernamePasswordAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.streaming.HttpStreamingType;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.notification.NotificationActionDefinition;
import org.mule.runtime.extension.api.notification.NotificationEmitter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.UnresolvedAddressException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component capable of performing an HTTP request given a request.
 *
 * @since 1.0
 */
public class HttpRequester {

  private static final Logger logger = LoggerFactory.getLogger(HttpRequester.class);

  private static int RETRY_ATTEMPTS = getInteger(RETRY_ATTEMPTS_PROPERTY, DEFAULT_RETRY_ATTEMPTS);
  private static boolean RETRY_ON_ALL_METHODS = getBoolean(RETRY_ON_ALL_METHODS_PROPERTY);

  private static final DataType REQUEST_NOTIFICATION_DATA_TYPE = DataType.fromType(HttpRequestNotificationData.class);
  private static final DataType RESPONSE_NOTIFICATION_DATA_TYPE = DataType.fromType(HttpResponseNotificationData.class);

  private static final HttpRequestFactory EVENT_TO_HTTP_REQUEST = new HttpRequestFactory();
  private static final HttpResponseToResult RESPONSE_TO_RESULT = new HttpResponseToResult();
  private static final HttpErrorMessageGenerator ERROR_MESSAGE_GENERATOR = new HttpErrorMessageGenerator();

  private static final Method fireNotificationMethod;

  static {
    Method fireLazy = null;
    try {
      fireLazy = NotificationEmitter.class.getDeclaredMethod("fireLazy", NotificationActionDefinition.class, Supplier.class,
                                                             DataType.class);
    } catch (NoSuchMethodException | SecurityException e) {
      // Nothing to do;
    }
    fireNotificationMethod = fireLazy;
  }

  public void doRequest(HttpExtensionClient client, HttpRequesterConfig config, String uri, String method,
                        HttpStreamingType streamingMode, HttpSendBodyMode sendBodyMode,
                        boolean followRedirects, HttpRequestAuthentication authentication,
                        int responseTimeout, ResponseValidator responseValidator,
                        TransformationService transformationService, HttpRequesterRequestBuilder requestBuilder,
                        boolean checkRetry, MuleContext muleContext, Scheduler scheduler, NotificationEmitter notificationEmitter,
                        StreamingHelper streamingHelper, CompletionCallback<InputStream, HttpResponseAttributes> callback) {
    doRequestWithRetry(client, config, uri, method, streamingMode, sendBodyMode, followRedirects, authentication, responseTimeout,
                       responseValidator, transformationService, requestBuilder, checkRetry, muleContext, scheduler,
                       notificationEmitter, streamingHelper, callback,
                       EVENT_TO_HTTP_REQUEST.create(config, uri, method, streamingMode, sendBodyMode, transformationService,
                                                    requestBuilder, authentication),
                       RETRY_ATTEMPTS);
  }

  private void doRequestWithRetry(HttpExtensionClient client, HttpRequesterConfig config, String uri, String method,
                                  HttpStreamingType streamingMode, HttpSendBodyMode sendBodyMode,
                                  boolean followRedirects, HttpRequestAuthentication authentication,
                                  int responseTimeout, ResponseValidator responseValidator,
                                  TransformationService transformationService, HttpRequesterRequestBuilder requestBuilder,
                                  boolean checkRetry, MuleContext muleContext, Scheduler scheduler,
                                  NotificationEmitter notificationEmitter,
                                  StreamingHelper streamingHelper,
                                  CompletionCallback<InputStream, HttpResponseAttributes> callback, HttpRequest httpRequest,
                                  int retryCount) {
    fireNotification(notificationEmitter, REQUEST_START, () -> HttpRequestNotificationData.from(httpRequest),
                     REQUEST_NOTIFICATION_DATA_TYPE);

    client.send(httpRequest, responseTimeout, followRedirects, resolveAuthentication(authentication))
        .whenComplete((response, exception) -> {
          if (response != null) {
            try {
              fireNotification(notificationEmitter, REQUEST_COMPLETE, () -> HttpResponseNotificationData.from(response),
                               RESPONSE_NOTIFICATION_DATA_TYPE);

              Result<InputStream, HttpResponseAttributes> result =
                  RESPONSE_TO_RESULT.convert(config, muleContext, response, httpRequest.getUri());

              resendRequest(result, checkRetry, authentication, () -> {
                scheduler.submit(() -> consumePayload(result));
                doRequest(client, config, uri, method, streamingMode, sendBodyMode, followRedirects,
                          authentication, responseTimeout, responseValidator, transformationService,
                          requestBuilder, false, muleContext, scheduler, notificationEmitter,
                          streamingHelper, callback);
              }, () -> {
                responseValidator.validate(result, httpRequest, streamingHelper);
                callback.success(result);
              });
            } catch (Exception e) {
              callback.error(e);
            }
          } else {
            checkIfRemotelyClosed(exception, client.getDefaultUriParameters());

            if (shouldRetryRemotelyClosed(exception, retryCount, httpRequest.getMethod())) {
              doRequestWithRetry(client, config, uri, method, streamingMode, sendBodyMode, followRedirects,
                                 authentication, responseTimeout, responseValidator, transformationService,
                                 requestBuilder, checkRetry, muleContext, scheduler, notificationEmitter,
                                 streamingHelper, callback,
                                 httpRequest, retryCount - 1);
              return;
            }

            logger.error(getErrorMessage(httpRequest));
            HttpError error = exception instanceof TimeoutException ? TIMEOUT : CONNECTIVITY;
            callback.error(new HttpRequestFailedException(createStaticMessage(ERROR_MESSAGE_GENERATOR
                .createFrom(httpRequest,
                            getExceptionMessage(exception))),
                                                          exception, error));
          }
        });
  }

  private String getExceptionMessage(Throwable t) {
    return getExceptionMessage(t, new HashSet<>());
  }

  private String getExceptionMessage(Throwable t, Set<Throwable> causes) {
    if (causes.add(t)) {
      if (t.getMessage() != null) {
        return t.getMessage();
      }

      if (t instanceof UnresolvedAddressException) {
        return "Couldn't resolve address";
      }

      if (t.getCause() != null) {
        return getExceptionMessage(t.getCause(), causes);
      }
    }
    return t.getClass().getSimpleName();
  }

  private void fireNotification(NotificationEmitter notificationEmitter, NotificationActionDefinition action,
                                Supplier<?> data, DataType dataType) {
    if (fireNotificationMethod != null) {
      try {
        fireNotificationMethod.invoke(notificationEmitter, action, data, dataType);
      } catch (InvocationTargetException e) {
        throw new MuleRuntimeException(e.getCause());
      } catch (IllegalAccessException | IllegalArgumentException e) {
        notificationEmitter.fire(action, new TypedValue(data.get(), dataType));
      }
    } else {
      notificationEmitter.fire(action, new TypedValue(data.get(), dataType));
    }
  }

  private String getErrorMessage(HttpRequest httpRequest) {
    return format("Error sending HTTP request to %s", httpRequest.getUri());
  }

  private void resendRequest(Result result, boolean retry, HttpRequestAuthentication authentication, Runnable retryCallback,
                             Runnable notRetryCallback) {
    if (retry && authentication != null) {
      authentication.retryIfShould(result, retryCallback, notRetryCallback);
    } else {
      notRetryCallback.run();
    }
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
    if (HTTPS.equals(uriParameters.getScheme()) && containsIgnoreCase(exception.getMessage(), REMOTELY_CLOSED)) {
      logger
          .error(
                 "Remote host closed connection. Possible SSL/TLS handshake issue. Check protocols, cipher suites and certificate set up. Use -Djavax.net.debug=ssl for further debugging.");
    }
  }

  private boolean shouldRetryRemotelyClosed(Throwable exception, int retryCount, String httpMethod) {
    boolean shouldRetry = exception instanceof IOException && containsIgnoreCase(exception.getMessage(), REMOTELY_CLOSED)
        && supportsRetry(httpMethod) && retryCount > 0;
    if (shouldRetry) {
      logger.warn("Sending HTTP message failed with `" + IOException.class.getCanonicalName() + ": " + REMOTELY_CLOSED
          + "`. Request will be retried " + retryCount + " time(s) before failing.");
    }
    return shouldRetry;
  }

  private boolean supportsRetry(String httpMethod) {
    return RETRY_ON_ALL_METHODS || IDEMPOTENT_METHODS.contains(httpMethod);
  }

  public static void refreshSystemProperties() {
    RETRY_ATTEMPTS = getInteger(RETRY_ATTEMPTS_PROPERTY, DEFAULT_RETRY_ATTEMPTS);
    RETRY_ON_ALL_METHODS = getBoolean(RETRY_ON_ALL_METHODS_PROPERTY);
  }

}
