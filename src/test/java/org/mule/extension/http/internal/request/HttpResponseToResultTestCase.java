/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.apache.tika.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpResponseToResultTestCase {

  private HttpResponseToResult httpResponseToResult;
  private HttpRequesterCookieConfig config;
  private MuleContext muleContext;
  private HttpResponse response;
  private HttpEntity entity;
  private URI uri;

  @Before
  public void setUp() {
    config = mock(HttpRequesterConfig.class);
    when(config.isEnableCookies()).thenReturn(false);

    response = mock(HttpResponse.class);
    when(response.getHeaders()).thenReturn(new MultiMap<>());

    entity = mock(HttpEntity.class);

    httpResponseToResult = new HttpResponseToResult();
  }

  @Test
  public void testConvertReturnsResultWithSuppliersOutput_WhenConvertingAResponseToAResult() {
    // Given
    String dummyString = "dummy string";
    InputStream expected = IOUtils.toInputStream(dummyString);
    Supplier<InputStream> payloadSupplier = () -> expected;
    when(entity.getLength()).thenReturn(Optional.of((long) dummyString.length()));

    // When
    Result<InputStream, HttpResponseAttributes> result =
        httpResponseToResult.convert(config, muleContext, response, entity, payloadSupplier, uri);

    // Then
    assertThat(result.getOutput(), sameInstance(expected));
  }

}
