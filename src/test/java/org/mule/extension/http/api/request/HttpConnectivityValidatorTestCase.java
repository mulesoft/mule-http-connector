/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request;

import static java.lang.Integer.valueOf;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_CONNECTION_IDLE_TIMEOUT;
import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_MAX_CONNECTIONS;
import static org.mule.extension.http.internal.request.RequestConnectionParams.DEFAULT_RESPONSE_BUFFER_SIZE;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_TYPE;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.extension.http.api.request.validator.ResponseValidatorTypedException;
import org.mule.extension.http.internal.request.RequestConnectionParams;
import org.mule.extension.http.internal.request.client.HttpExtensionClient;
import org.mule.extension.socket.api.socket.tcp.TcpClientSocketProperties;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.HttpConstants.Protocol;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class HttpConnectivityValidatorTestCase {

  private HttpConnectivityValidator connectivityValidator;

  private HttpExtensionClient client;

  private RequestConnectionParams connectionParams;

  private HttpResponse validationResponse;

  @Rule
  public DynamicPort port = new DynamicPort("port");

  @Before
  public void setup() {
    validationResponse = mockResponse();

    client = mock(HttpExtensionClient.class);
    when(client.send(any(HttpRequest.class), anyInt(), anyBoolean(), any(HttpAuthentication.class)))
        .thenReturn(completedFuture(validationResponse));

    connectionParams = new RequestConnectionParams();
    connectionParams.setProtocol(Protocol.HTTP);
    connectionParams.setHost("localhost");
    connectionParams.setPort(port.getNumber());
    connectionParams.setUsePersistentConnections(true);
    connectionParams.setMaxConnections(valueOf(DEFAULT_MAX_CONNECTIONS));
    connectionParams.setConnectionIdleTimeout(valueOf(DEFAULT_CONNECTION_IDLE_TIMEOUT));
    connectionParams.setStreamResponse(false);
    connectionParams.setResponseBufferSize(valueOf(DEFAULT_RESPONSE_BUFFER_SIZE).intValue());
    connectionParams.setClientSocketProperties(mock(TcpClientSocketProperties.class));

    connectivityValidator = new HttpConnectivityValidator();
  }

  private HttpResponse mockResponse() {
    HttpEntity entity = mock(HttpEntity.class);

    String textPayload = "some text payload";
    InputStream payloadInputStream = IOUtils.toInputStream(textPayload);
    when(entity.getContent()).thenReturn(payloadInputStream);
    when(entity.getLength()).thenReturn(Optional.of((long) textPayload.length()));

    HttpResponse validationResponse = mock(HttpResponse.class);
    when(validationResponse.getStatusCode()).thenReturn(200);
    when(validationResponse.getEntity()).thenReturn(entity);
    when(validationResponse.getHeaders()).thenReturn(new MultiMap<>());
    when(validationResponse.getEntity()).thenReturn(entity);
    when(validationResponse.getHeaderValue(CONTENT_TYPE)).thenReturn("text/plain");

    return validationResponse;
  }

  @Test
  public void whenTheValidationResponseIsOkThenTheValidationSucceeds() throws ExecutionException, InterruptedException {
    connectivityValidator.validate(client, connectionParams);
  }

  @Test(expected = ResponseValidatorTypedException.class)
  public void whenTheValidationResponseIsAnErrorTheValidationFails() throws ExecutionException, InterruptedException {
    when(validationResponse.getStatusCode()).thenReturn(500);
    connectivityValidator.validate(client, connectionParams);
  }
}
