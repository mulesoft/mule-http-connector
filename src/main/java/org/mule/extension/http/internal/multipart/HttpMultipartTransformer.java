/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.multipart;

import static java.lang.Math.toIntExact;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.message.MultiPartPayload;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.transformer.Transformer;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.core.message.PartAttributes;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;

import java.util.Collection;

/**
 * Handles the transformation of {@link MultiPartPayload} to an {@link HttpPart} collection.
 *
 * @since 1.0
 */
public class HttpMultipartTransformer {

  public static Collection<HttpPart> createFrom(MultiPartPayload multiPartPayload, Transformer objectToByteArray) {
    return multiPartPayload.getParts().stream().map(message -> {
      PartAttributes partAttributes = (PartAttributes) message.getAttributes().getValue();
      TypedValue<Object> payload = message.getPayload();
      String name = partAttributes.getName();
      byte[] data;
      try {
        data = (byte[]) objectToByteArray.transform(payload.getValue());
        String fileName = partAttributes.getFileName();
        String contentType = payload.getDataType().getMediaType().toRfcString();
        int size = toIntExact(partAttributes.getSize());
        if (fileName != null) {
          return new HttpPart(name, fileName, data, contentType, size);
        } else {
          return new HttpPart(name, data, contentType, size);
        }
      } catch (TransformerException e) {
        throw new MuleRuntimeException(createStaticMessage(format("Could not create HTTP part '%s'", name), e));
      }
    }).collect(toList());
  }

}
