/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.sse;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.http.api.SseClientAttributes;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.sdk.api.http.HttpServer;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

@Alias("sseEndpoint")
@MediaType(value = ANY, strict = false)
public class SseEndpoint extends Source<Void, SseClientAttributes> {

  private static final Logger LOGGER = getLogger(SseEndpoint.class);

  @Connection
  private ConnectionProvider<HttpServer> serverProvider;

  @Inject
  @Named("_sseClientsRepository")
  private SseClientsRepository sseClientsRepository;

  @Parameter
  @Placement(order = 1)
  @Optional(defaultValue = "/")
  private String path = "/";


  @Override
  public void onStart(SourceCallback<Void, SseClientAttributes> sourceCallback) throws MuleException {
    HttpServer server = serverProvider.connect();
    server.sse(path, new PassClientToFlow(sourceCallback, sseClientsRepository));
  }

  @Override
  public void onStop() {
    // TODO: implement
  }
}
