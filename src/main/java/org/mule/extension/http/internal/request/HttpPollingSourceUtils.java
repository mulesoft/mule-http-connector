/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static org.mule.extension.http.internal.request.HttpPollingSource.ATTRIBUTES_PLACEHOLDER;
import static org.mule.extension.http.internal.request.HttpPollingSource.ITEM_PLACEHOLDER;
import static org.mule.extension.http.internal.request.HttpPollingSource.PAYLOAD_PLACEHOLDER;
import static org.mule.extension.http.internal.request.HttpPollingSource.WATERMARK_PLACEHOLDER;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.metadata.DataType.STRING;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.el.ExpressionLanguage;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utils to be used for resolution of expressions of the {@link HttpPollingSource}
 * @since 1.7
 */
public final class HttpPollingSourceUtils {

  private HttpPollingSourceUtils() {
    // Empty private constructor to avoid instantiation.
  }

  private static BindingContext buildContext(Optional<TypedValue<String>> payload, Optional<HttpResponseAttributes> attributes,
                                             Serializable currentWatermark, Optional<TypedValue> item) {
    BindingContext.Builder builder = BindingContext.builder();
    payload.ifPresent(p -> builder.addBinding(PAYLOAD_PLACEHOLDER, p));
    attributes.ifPresent(attr -> builder.addBinding(ATTRIBUTES_PLACEHOLDER, TypedValue.of(attr)));
    builder.addBinding(WATERMARK_PLACEHOLDER, TypedValue.of(currentWatermark));
    item.ifPresent(it -> builder.addBinding(ITEM_PLACEHOLDER, it));
    return builder.build();
  }

  private static Result<TypedValue<?>, HttpResponseAttributes> toResult(TypedValue<?> item, MediaType mediaType,
                                                                        HttpResponseAttributes attributes) {
    return Result.<TypedValue<?>, HttpResponseAttributes>builder().attributes(attributes).output(item).mediaType(mediaType)
        .build();
  }

  private static boolean isJavaPayload(MediaType mediaType) {
    return mediaType.matches(APPLICATION_JAVA);
  }

  /**
   * Returns the items withing the payload (and/or attributes) of the response, considering the itemExpression.
   * application/java is not an accepted {@link MediaType}
   *
   * @param payload
   * @param attributes
   * @param currentWatermark
   * @param itemsExpression
   * @param expressionLanguage
   * @return A {@link Stream} of {@link Result}s, after being splitted given the itemExpression. If there is no
   * itemExpression (i.e. empty {@link Optional}), the result will just contain the current payload. If there is
   * an expression, it will use the {@link ExpressionLanguage} to use it to split the payload and return a stream of
   * {@link Result}s.
   */
  public static Stream<Result<TypedValue<?>, HttpResponseAttributes>> getItems(TypedValue<String> payload,
                                                                               HttpResponseAttributes attributes,
                                                                               Serializable currentWatermark,
                                                                               Optional<String> itemsExpression,
                                                                               ExpressionLanguage expressionLanguage) {
    if (isJavaPayload(payload.getDataType().getMediaType())) {
      throw new MuleRuntimeException(createStaticMessage(format("%s is not an accepted media type",
                                                                APPLICATION_JAVA.toRfcString())));
    }
    if (!itemsExpression.isPresent()) {
      return Stream.of(toResult(payload, payload.getDataType().getMediaType(), attributes));
    }

    Iterable<TypedValue<?>> splitted =
        () -> expressionLanguage.split(itemsExpression.get(),
                                       buildContext(Optional.of(payload), Optional.of(attributes), currentWatermark, empty()));
    return StreamSupport.stream(splitted.spliterator(), false)
        .map(item -> toResult(item, item.getDataType().getMediaType(), attributes));
  }

  /**
   * @param payload
   * @param watermarkExpression
   * @param currentWatermark
   * @param item
   * @param expressionLanguage
   * @return Given a payload, current watermark, and current item (payload and attributes of the response), uses the
   * watermark expression to extract the new watermark value from the payload, item and or attributes.
   */
  public static Serializable getItemWatermark(TypedValue<String> payload, String watermarkExpression,
                                              Serializable currentWatermark, Result<TypedValue<?>, HttpResponseAttributes> item,
                                              ExpressionLanguage expressionLanguage) {
    return (Serializable) resolveExpression(payload, watermarkExpression, currentWatermark, item, expressionLanguage).getValue();
  }

  /**
   * @param payload
   * @param idExpression
   * @param currentWatermark
   * @param item
   * @param expressionLanguage
   * @return Given a payload, current watermark, and current item (payload and attributes of the response), uses the
   * id expression to extract the item's id value from the item (and potentially payload and/or attributes).
   */
  public static String getItemId(TypedValue<String> payload, String idExpression, Serializable currentWatermark,
                                 Result<TypedValue<?>, HttpResponseAttributes> item, ExpressionLanguage expressionLanguage) {
    return (String) resolveExpression(payload, idExpression, currentWatermark, item, expressionLanguage).getValue();
  }

  private static TypedValue<?> resolveExpression(TypedValue<String> payload, String idExpression, Serializable currentWatermark,
                                                 Result<TypedValue<?>, HttpResponseAttributes> item,
                                                 ExpressionLanguage expressionLanguage) {
    return expressionLanguage
        .evaluate(idExpression, STRING,
                  buildContext(Optional.of(payload), item.getAttributes(), currentWatermark, Optional.of(item.getOutput())));
  }

}
