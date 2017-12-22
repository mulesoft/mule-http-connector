/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.mule.extension.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.extension.http.api.error.HttpError.BASIC_AUTHENTICATION;
import static org.mule.extension.http.api.error.HttpError.NOT_FOUND;
import static org.mule.extension.http.internal.HttpConnectorConstants.RESPONSE;
import static org.mule.extension.http.internal.listener.HttpRequestToResult.transform;
import static org.mule.runtime.api.component.ComponentIdentifier.builder;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.metadata.DataType.STRING;
import static org.mule.runtime.core.api.exception.Errors.ComponentIdentifiers.Handleable.SECURITY;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static org.mule.runtime.core.api.util.SystemUtils.getDefaultEncoding;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.DROP;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.BAD_REQUEST;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.DROPPED;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.getReasonPhraseForStatusCode;
import static org.mule.runtime.http.api.HttpHeaders.Names.X_CORRELATION_ID;
import static org.slf4j.LoggerFactory.getLogger;
import org.mule.extension.http.api.HttpListenerResponseAttributes;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.listener.builder.HttpListenerErrorResponseBuilder;
import org.mule.extension.http.api.listener.builder.HttpListenerSuccessResponseBuilder;
import org.mule.extension.http.api.listener.server.HttpListenerConfig;
import org.mule.extension.http.internal.HttpStreamingType;
import org.mule.extension.http.internal.listener.intercepting.InterceptingException;
import org.mule.extension.http.internal.listener.server.ModuleRequestHandler;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.message.Error;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.exception.DisjunctiveErrorTypeMatcher;
import org.mule.runtime.core.api.exception.ErrorTypeMatcher;
import org.mule.runtime.core.api.exception.SingleErrorTypeMatcher;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Streaming;
import org.mule.runtime.extension.api.annotation.execution.OnError;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.execution.OnTerminate;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.source.BackPressure;
import org.mule.runtime.extension.api.annotation.source.EmitsResponse;
import org.mule.runtime.extension.api.annotation.source.OnBackPressure;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.BackPressureAction;
import org.mule.runtime.extension.api.runtime.source.BackPressureContext;
import org.mule.runtime.extension.api.runtime.source.BackPressureMode;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.mule.runtime.extension.api.runtime.source.SourceCompletionCallback;
import org.mule.runtime.extension.api.runtime.source.SourceResult;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import org.mule.runtime.http.api.domain.HttpProtocol;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.http.api.domain.message.response.HttpResponseBuilder;
import org.mule.runtime.http.api.domain.request.HttpRequestContext;
import org.mule.runtime.http.api.server.HttpServer;
import org.mule.runtime.http.api.server.RequestHandler;
import org.mule.runtime.http.api.server.RequestHandlerManager;
import org.mule.runtime.http.api.server.async.HttpResponseReadyCallback;
import org.mule.runtime.http.api.server.async.ResponseStatusCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Represents a listener for HTTP requests.
 *
 * @since 1.0
 */
@Alias("listener")
@EmitsResponse
@Streaming
@MediaType(value = ANY, strict = false)
@BackPressure(defaultMode = BackPressureMode.FAIL, supportedModes = {BackPressureMode.FAIL, DROP})
public class HttpListener extends Source<InputStream, HttpRequestAttributes> {

  private static final String RESPONSE_SEND_ATTEMPT = "responseSendAttempt";

  public static final String HTTP_NAMESPACE = "http";
  private static final Logger LOGGER = getLogger(HttpListener.class);
  private static final String SERVER_PROBLEM = "Server encountered a problem";
  private static final String RESPONSE_CONTEXT = "responseContext";
  private static final String RESPONSE_CONTEXT_NOT_FOUND = "Response Context is not present. Could not send response.";

  @Inject
  private TransformationService transformationService;

  @Inject
  private MuleContext muleContext;

  @Config
  private HttpListenerConfig config;

  @Connection
  private ConnectionProvider<HttpServer> serverProvider;

  /**
   * Relative path from the path set in the HTTP Listener configuration
   */
  @Parameter
  @Placement(order = 1)
  private String path;

  /**
   * Comma separated list of allowed HTTP methods by this listener. To allow all methods do not defined the attribute.
   */
  @Parameter
  @Optional
  @Placement(tab = Placement.ADVANCED_TAB)
  @Summary("Comma separated list of methods. Leave empty to allow all.")
  @Example("GET, POST")
  private String allowedMethods;

  /**
   * Defines if the response should be sent using streaming or not. If this attribute is not present, the behavior will depend on
   * the type of the payload (it will stream only for InputStream). If set to true, it will always stream. If set to false, it
   * will never stream. As streaming is done the response will be sent user Transfer-Encoding: chunked.
   */
  @Parameter
  @Optional(defaultValue = "AUTO")
  @Placement(tab = ADVANCED_TAB)
  private HttpStreamingType responseStreamingMode;

  private HttpServer server;
  private HttpListenerResponseSender responseSender;
  private ListenerPath listenerPath;
  private RequestHandlerManager requestHandlerManager;
  private HttpResponseFactory responseFactory;
  private ErrorTypeMatcher knownErrors;
  private Class interpretedAttributes;

  // TODO: MULE-10900 figure out a way to have a shared group between callbacks and possibly regular params
  @OnSuccess
  public void onSuccess(@ParameterGroup(name = RESPONSE,
      showInDsl = true) HttpListenerSuccessResponseBuilder response,
                        SourceCallbackContext callbackContext,
                        SourceCompletionCallback completionCallback)
      throws Exception {

    HttpResponseContext context = callbackContext.<HttpResponseContext>getVariable(RESPONSE_CONTEXT)
        .orElseThrow(() -> new MuleRuntimeException(createStaticMessage(RESPONSE_CONTEXT_NOT_FOUND)));

    responseSender.sendResponse(context, response, completionCallback);
  }

  // TODO: MULE-10900 figure out a way to have a shared group between callbacks and possibly regular params
  @OnError
  public void onError(@ParameterGroup(name = "Error Response",
      showInDsl = true) HttpListenerErrorResponseBuilder errorResponse,
                      SourceCallbackContext callbackContext,
                      Error error,
                      SourceCompletionCallback completionCallback) {
    try {
      sendErrorResponse(errorResponse, callbackContext, error, completionCallback);
    } catch (Throwable t) {
      completionCallback.error(t);
    }
  }

  @OnBackPressure
  public void onBackPressure(BackPressureContext ctx, SourceCompletionCallback completionCallback) {
    try {
      sendBackPressureResponse(ctx, completionCallback);
    } catch (Throwable t) {
      completionCallback.error(t);
    }
  }

  @OnTerminate
  public void onTerminate(SourceResult sourceResult) {
    Boolean sendingResponse = (Boolean) sourceResult.getSourceCallbackContext().getVariable(RESPONSE_SEND_ATTEMPT).orElse(false);
    if (FALSE.equals(sendingResponse)) {
      sourceResult
          .getInvocationError()
          .ifPresent(error -> sendErrorResponse(new HttpListenerErrorResponseBuilder(),
                                                sourceResult.getSourceCallbackContext(),
                                                error,
                                                null));
    }
  }

  private void sendErrorResponse(HttpListenerErrorResponseBuilder errorResponse,
                                 SourceCallbackContext callbackContext,
                                 Error error,
                                 SourceCompletionCallback completionCallback) {

    final HttpResponseBuilder failureResponseBuilder = createFailureResponseBuilder(error);

    if (errorResponse.getBody() == null || errorResponse.getBody().getValue() == null) {
      errorResponse.setBody(new TypedValue<>(error.getDescription(), STRING));
    }

    HttpResponseContext context = callbackContext.<HttpResponseContext>getVariable(RESPONSE_CONTEXT)
        .orElseThrow(() -> new MuleRuntimeException(createStaticMessage(RESPONSE_CONTEXT_NOT_FOUND)));

    HttpResponse response;
    try {
      response = responseFactory
          .create(failureResponseBuilder, context.getInterception(), errorResponse, context.isSupportStreaming());
    } catch (Exception e) {
      response = buildErrorResponse();
    }

    final HttpResponseReadyCallback responseCallback = context.getResponseCallback();
    callbackContext.addVariable(RESPONSE_SEND_ATTEMPT, true);
    responseCallback.responseReady(response, getResponseFailureCallback(responseCallback, completionCallback));
  }

  private void sendBackPressureResponse(BackPressureContext ctx, SourceCompletionCallback completionCallback) {
    final SourceCallbackContext callbackContext = ctx.getSourceCallbackContext();
    final HttpResponseContext context = callbackContext.<HttpResponseContext>getVariable(RESPONSE_CONTEXT)
        .orElseThrow(() -> new MuleRuntimeException(createStaticMessage(RESPONSE_CONTEXT_NOT_FOUND)));

    HttpStatus responseStatus = ctx.getAction() == BackPressureAction.FAIL ? SERVICE_UNAVAILABLE : DROPPED;
    HttpResponseBuilder responseBuilder = HttpResponse.builder().statusCode(responseStatus.getStatusCode());
    HttpListenerErrorResponseBuilder errorResponseBuilder = new HttpListenerErrorResponseBuilder();
    errorResponseBuilder.setBody(new TypedValue<>(null, STRING));
    errorResponseBuilder.setStatusCode(responseStatus.getStatusCode());
    errorResponseBuilder.setReasonPhrase(responseStatus.getReasonPhrase());

    HttpResponse response;
    try {
      response = responseFactory
          .create(responseBuilder, context.getInterception(), errorResponseBuilder, context.isSupportStreaming());
    } catch (Exception e) {
      response = buildErrorResponse();
    }

    final HttpResponseReadyCallback responseCallback = context.getResponseCallback();
    callbackContext.addVariable(RESPONSE_SEND_ATTEMPT, true);
    responseCallback.responseReady(response, getResponseFailureCallback(responseCallback, completionCallback));
  }

  private HttpResponseBuilder createFailureResponseBuilder(Error error) {
    final HttpResponseBuilder failureResponseBuilder;
    if (hasCustomResponse(ofNullable(error))) {
      Message errorMessage = error.getErrorMessage();
      HttpResponseAttributes attributes = (HttpResponseAttributes) errorMessage.getAttributes().getValue();
      failureResponseBuilder = HttpResponse.builder()
          .statusCode(attributes.getStatusCode())
          .reasonPhrase(attributes.getReasonPhrase());
      attributes.getHeaders().forEach(failureResponseBuilder::addHeader);
    } else if (error != null) {
        failureResponseBuilder = createDefaultFailureResponseBuilder(error, INTERNAL_SERVER_ERROR);
    } else {
      failureResponseBuilder = HttpResponse.builder();
    }
    return failureResponseBuilder;
  }

  @Override
  public void onStart(SourceCallback<InputStream, HttpRequestAttributes> sourceCallback) throws MuleException {
    server = serverProvider.connect();
    listenerPath = config.getFullListenerPath(config.sanitizePathWithStartSlash(path));
    path = listenerPath.getResolvedPath();
    responseFactory =
        new HttpResponseFactory(responseStreamingMode, transformationService);
    responseSender = new HttpListenerResponseSender(responseFactory);
    startIfNeeded(responseFactory);

    validatePath();
    interpretedAttributes = HttpListenerResponseAttributes.class;

    try {
      if (allowedMethods != null) {
        requestHandlerManager =
            server.addRequestHandler(asList(extractAllowedMethods()), path, getRequestHandler(sourceCallback));
      } else {
        requestHandlerManager =
            server.addRequestHandler(path, getRequestHandler(sourceCallback));
      }
    } catch (Exception e) {
      throw new MuleRuntimeException(e);
    }
    knownErrors = new DisjunctiveErrorTypeMatcher(createErrorMatcherList(muleContext.getErrorTypeRepository()));
    requestHandlerManager.start();
  }

  private List<ErrorTypeMatcher> createErrorMatcherList(ErrorTypeRepository errorTypeRepository) {
    List<ErrorTypeMatcher> matchers = new LinkedList<>();
    matchers.add(new SingleErrorTypeMatcher(errorTypeRepository.lookupErrorType(SECURITY).get()));
    ComponentIdentifier.Builder httpErrorBuilder = builder().namespace(HTTP_NAMESPACE.toUpperCase());
    matchers.add(new SingleErrorTypeMatcher(errorTypeRepository.lookupErrorType(httpErrorBuilder
        .name(NOT_FOUND.name())
        .build()).get()));
    matchers.add(new SingleErrorTypeMatcher(errorTypeRepository.lookupErrorType(httpErrorBuilder
        .name(BASIC_AUTHENTICATION.name())
        .build()).get()));
    return matchers;
  }

  @Override
  public void onStop() {
    if (requestHandlerManager != null) {
      requestHandlerManager.stop();
      requestHandlerManager.dispose();
    }

    if (server != null) {
      serverProvider.disconnect(server);
    }
  }

  private RequestHandler getRequestHandler(SourceCallback<InputStream, HttpRequestAttributes> sourceCallback) {
    return new ModuleRequestHandler() {

      @Override
      public Result<InputStream, HttpRequestAttributes> createResult(HttpRequestContext requestContext) {
        return HttpListener.this.createResult(requestContext);
      }

      @Override
      public void handleRequest(HttpRequestContext requestContext, HttpResponseReadyCallback responseCallback) {
        // TODO: MULE-9698 Analyse adding security here to reject the DefaultHttpRequestContext and avoid creating a Message
        try {
          Result<InputStream, HttpRequestAttributes> result = createResult(requestContext);

          HttpResponseContext responseContext = new HttpResponseContext();
          final String httpVersion = requestContext.getRequest().getProtocol().asString();
          responseContext.setHttpVersion(httpVersion);
          responseContext.setSupportStreaming(supportsTransferEncoding(httpVersion));
          responseContext.setResponseCallback(responseCallback);
          MultiMap<String, String> headers = getHeaders(result);
          config.getInterceptor().ifPresent(interceptor -> responseContext
              .setInterception(interceptor.request(getMethod(result), headers)));

          SourceCallbackContext context = sourceCallback.createContext();
          context.addVariable(RESPONSE_CONTEXT, responseContext);

          String correlationId = headers.get(X_CORRELATION_ID.toLowerCase());
          if (correlationId != null) {
            context.setCorrelationId(correlationId);
          }

          sourceCallback.handle(result, context);
        } catch (IllegalArgumentException e) {
          LOGGER.warn("Exception occurred parsing request:", e);
          sendErrorResponse(BAD_REQUEST, e.getMessage(), responseCallback);
        } catch (InterceptingException e) {
          sendErrorResponse(e, responseCallback);
        } catch (RuntimeException e) {
          LOGGER.warn("Exception occurred processing request:", e);
          sendErrorResponse(INTERNAL_SERVER_ERROR, SERVER_PROBLEM, responseCallback);
        }
      }

      private String getMethod(Result<InputStream, HttpRequestAttributes> result) {
        return result.getAttributes().get().getMethod();
      }

      private MultiMap<String, String> getHeaders(Result<InputStream, HttpRequestAttributes> result) {
        return result.getAttributes().get().getHeaders();
      }

      private void sendErrorResponse(InterceptingException interceptor, HttpResponseReadyCallback responseCallback) {
        HttpStatus status = interceptor.status();

        HttpResponseBuilder responseBuilder = HttpResponse.builder()
            .statusCode(status.getStatusCode())
            .reasonPhrase(status.getReasonPhrase());

        interceptor.headers().entryList()
            .forEach(entry -> responseBuilder.addHeader(entry.getKey(), entry.getValue()));

        responseCallback.responseReady(responseBuilder
            .build(), new ResponseStatusCallback() {

              @Override
              public void responseSendFailure(Throwable exception) {
                logError(status, exception);
              }

              @Override
              public void responseSendSuccessfully() {}
            });
      }

      private void sendErrorResponse(final HttpStatus status, String message,
                                     HttpResponseReadyCallback responseCallback) {
        byte[] responseData = message.getBytes();
        responseCallback.responseReady(HttpResponse.builder()
            .statusCode(status.getStatusCode())
            .reasonPhrase(status.getReasonPhrase())
            .entity(new ByteArrayHttpEntity(responseData))
            .addHeader(CONTENT_LENGTH, Integer.toString(responseData.length))
            .build(), new ResponseStatusCallback() {

              @Override
              public void responseSendFailure(Throwable exception) {
                logError(status, exception);
              }

              @Override
              public void responseSendSuccessfully() {}
            });
      }

      private void logError(HttpStatus status, Throwable exception) {
        LOGGER.warn("Error while sending {} response {}", status.getStatusCode(), exception.getMessage());
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Exception thrown", exception);
        }
      }

    };
  }

  private boolean hasCustomResponse(java.util.Optional<Error> error) {
    return error.isPresent() && knownErrors.match(error.get().getErrorType()) && error.get().getErrorMessage() != null
        && interpretedAttributes.isInstance(error.get().getErrorMessage().getAttributes().getValue());
  }

  private HttpResponseBuilder createDefaultFailureResponseBuilder(Error error, HttpStatus httpStatus) {
    // Default to the HTTP transport exception mapping for compatibility
    Throwable throwable = error.getCause();
    String reasonPhraseFromException = getReasonPhraseForStatusCode(httpStatus.getStatusCode());
    return HttpResponse.builder()
        .statusCode(httpStatus.getStatusCode())
        .reasonPhrase(reasonPhraseFromException != null ? reasonPhraseFromException : throwable.getMessage());
  }

  private Result<InputStream, HttpRequestAttributes> createResult(HttpRequestContext requestContext) {
    return transform(requestContext, getDefaultEncoding(muleContext), listenerPath);
    // TODO: MULE-9748 Analyse RequestContext use in HTTP extension
    // Update RequestContext ThreadLocal for backwards compatibility
    // setCurrentEvent(muleEvent);
    // return muleEvent;
  }

  protected HttpResponse buildErrorResponse() {
    final HttpResponseBuilder errorResponseBuilder = HttpResponse.builder();
    final HttpResponse errorResponse = errorResponseBuilder.statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
        .reasonPhrase(INTERNAL_SERVER_ERROR.getReasonPhrase())
        .build();
    return errorResponse;
  }

  private ResponseStatusCallback getResponseFailureCallback(HttpResponseReadyCallback responseReadyCallback,
                                                            SourceCompletionCallback completionCallback) {
    return new ResponseStatusCallback() {

      @Override
      public void responseSendFailure(Throwable throwable) {
        responseReadyCallback.responseReady(buildErrorResponse(), this);
        if (completionCallback != null) {
          completionCallback.error(throwable);
        }
      }

      @Override
      public void responseSendSuccessfully() {
        // TODO: MULE-9749 Figure out how to handle this. Maybe doing nothing is right since this will be executed later if
        // everything goes right.
        // responseCompletationCallback.responseSentSuccessfully();
        if (completionCallback != null) {
          completionCallback.success();
        }
      }
    };
  }

  private boolean supportsTransferEncoding(String httpVersion) {
    return !(HttpProtocol.HTTP_0_9.asString().equals(httpVersion) || HttpProtocol.HTTP_1_0.asString().equals(httpVersion));
  }

  private String[] extractAllowedMethods() throws InitialisationException {
    final String[] values = this.allowedMethods.split(",");
    final String[] normalizedValues = new String[values.length];
    int normalizedValueIndex = 0;
    for (String value : values) {
      normalizedValues[normalizedValueIndex] = value.trim().toUpperCase();
      normalizedValueIndex++;
    }
    return normalizedValues;
  }

  private void validatePath() throws MuleException {
    final String[] pathParts = this.path.split("/");
    List<String> uriParamNames = new ArrayList<>();
    for (String pathPart : pathParts) {
      if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
        String uriParamName = pathPart.substring(1, pathPart.length() - 1);
        if (uriParamNames.contains(uriParamName)) {
          throw new DefaultMuleException(
                                         createStaticMessage(format(
                                                                    "Http Listener with path %s contains duplicated uri param names",
                                                                    this.path)));
        }
        uriParamNames.add(uriParamName);
      } else {
        if (pathPart.contains("*") && pathPart.length() > 1) {
          throw new DefaultMuleException(createStaticMessage(format(
                                                                    "Http Listener with path %s contains an invalid use of a wildcard. Wildcards can only be used at the end of the path (i.e.: /path/*) or between / characters (.i.e.: /path/*/anotherPath))",
                                                                    this.path)));
        }
      }
    }
  }

}
