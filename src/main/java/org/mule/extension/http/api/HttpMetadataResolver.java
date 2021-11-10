/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.AnyType;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.resolving.OutputStaticTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;

/**
 * HTTP {@link OutputTypeResolver} implementation for the basic operations that always return an {@link AnyType}, since HTTP
 * may return any object type like an XML, Json, Multipart, etc.
 *
 * @since 1.0
 */
public class HttpMetadataResolver extends OutputStaticTypeResolver {

  private static final AnyType ANY_TYPE = BaseTypeBuilder.create(JAVA).anyType().build();

  @Override
  public String getCategoryName() {
    return "HTTP";
  }

  @Override
  public MetadataType getStaticMetadata() {
    return ANY_TYPE;
  }
}
