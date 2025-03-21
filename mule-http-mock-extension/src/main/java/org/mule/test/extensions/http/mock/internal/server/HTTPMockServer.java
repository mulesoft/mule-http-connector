/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal.server;


import static org.mule.test.extensions.http.mock.internal.server.DelegateToFlowTransformer.SOURCE_CALLBACK_WIREMOCK_PARAMETER;
import static org.mule.test.extensions.http.mock.internal.server.DelegateToFlowTransformer.TRANSFORMER_NAME;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.mule.runtime.extension.api.runtime.source.SourceCallback;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * WireMock-implemented HTTP Server.
 */
public final class HTTPMockServer {

  private final WireMockServer wireMockServer;

  public HTTPMockServer(int port) {
    wireMockServer = new WireMockServer(wireMockConfig().port(port).extensions(new DelegateToFlowTransformer()));
    wireMockServer.start();
  }

  public void invalidate() {
    wireMockServer.stop();
  }

  public StubRemover addHandlerFor(String path, SourceCallback<?, ?> sourceCallback) {
    StubMapping stubMapping = wireMockServer.stubFor(WireMock.any(urlPathEqualTo(path))
        .willReturn(aResponse().withTransformer(TRANSFORMER_NAME, SOURCE_CALLBACK_WIREMOCK_PARAMETER, sourceCallback)));
    return new StubRemover(stubMapping, wireMockServer);
  }

  public static class StubRemover {

    private final StubMapping stubMapping;
    private final WireMockServer wireMockServer;

    public StubRemover(StubMapping stubMapping, WireMockServer wireMockServer) {
      this.stubMapping = stubMapping;
      this.wireMockServer = wireMockServer;
    }

    public void removeStub() {
      wireMockServer.removeStubMapping(stubMapping);
    }
  }
}
