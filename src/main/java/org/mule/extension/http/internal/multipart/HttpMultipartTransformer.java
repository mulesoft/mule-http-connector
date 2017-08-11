/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.multipart;

import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;

/**
 * Handles the transformation of MultiPartPayload to an {@link HttpPart} collection.
 *
 * @since 1.0
 */
//TODO: MULE-12985 - Move multipart test cases to HTTP service and adapt extension ones
public class HttpMultipartTransformer {

  //  public static Collection<HttpPart> createFrom(MultiPartPayload multiPartPayload, TransformationService transformationService) {
  //    return multiPartPayload.getParts().stream().map(message -> {
  //      PartAttributes partAttributes = (PartAttributes) message.getAttributes().getValue();
  //      TypedValue<Object> payload = message.getPayload();
  //      String name = partAttributes.getName();
  //      byte[] data;
  //      data = (byte[]) transformationService.transform(payload.getValue(), payload.getDataType(), BYTE_ARRAY);
  //      String fileName = partAttributes.getFileName();
  //      String contentType = payload.getDataType().getMediaType().toRfcString();
  //      int size = toIntExact(partAttributes.getSize());
  //      if (fileName != null) {
  //        return new HttpPart(name, fileName, data, contentType, size);
  //      } else {
  //        return new HttpPart(name, data, contentType, size);
  //      }
  //    }).collect(toList());
  //  }

}
