/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.extension.http.internal.request.profiling.HttpRequestProfilingEventContext.REASON_PHRASE;
import static org.mule.extension.http.internal.request.profiling.HttpRequestProfilingEventContext.STATUS_CODE;
import static org.mule.extension.http.internal.HttpConnectorConstants.HTTP_ENABLE_PROFILING;
import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.PROFILING;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.mule.runtime.api.profiling.ProfilingDataProducer;
import org.mule.runtime.api.profiling.ProfilingEventContext;
import org.mule.runtime.api.profiling.ProfilingService;
import org.mule.runtime.api.profiling.type.ProfilingEventType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Request;
import org.junit.Rule;
import org.junit.Test;
import org.mule.runtime.api.profiling.type.context.ExtensionProfilingEventContext;
import org.mule.tck.junit4.rule.SystemProperty;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Feature(HTTP_EXTENSION)
@Story(PROFILING)
public class HttpRequestProfilingTestCase extends AbstractHttpRequestTestCase {

  public static final String BODY = "OK";

  @Rule
  public SystemProperty persistentConnection = new SystemProperty(HTTP_ENABLE_PROFILING, "true");

  private final TestProfilingDataProducer profilingDataProducer = new TestProfilingDataProducer();

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    Map<String, Object> registryObjects = new HashMap<>();
    registryObjects.put("test.profiling.service", new TestProfilingService(profilingDataProducer));
    return registryObjects;
  }

  @Override
  protected void handleRequest(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setStatus(SC_OK);
    response.getWriter().print(BODY);
  }

  @Override
  protected String getConfigFile() {
    return "http-request-profiling-config.xml";
  }

  @Test
  public void whenARequestIsSentTheResponseIsProfiled() throws Exception {
    runFlow("executeRequest").getMessage();
    ExtensionProfilingEventContext profilingEventContext = profilingDataProducer.getLastProfilingEventContext();
    assertThat(profilingEventContext, is(notNullValue()));
    assertThat(profilingEventContext.get(STATUS_CODE).orElse(null), equalTo(200));
    assertThat(profilingEventContext.get(REASON_PHRASE).orElse(null), equalTo(BODY));
  }

  /**
   * A test {@link ProfilingService}
   */
  private static final class TestProfilingService implements ProfilingService {

    private final ProfilingDataProducer profilingDataProducer;

    public TestProfilingService(ProfilingDataProducer profilingDataProducer) {
      this.profilingDataProducer = profilingDataProducer;
    }

    @Override
    public <T extends ProfilingEventContext> ProfilingDataProducer<T> getProfilingDataProducer(
                                                                                               ProfilingEventType<T> profilingEventType) {
      return profilingDataProducer;
    }

    @Override
    public <T extends ProfilingEventContext> void registerProfilingDataProducer(ProfilingEventType<T> profilingEventType,
                                                                                ProfilingDataProducer<T> profilingDataProducer) {
      // Nothing to to in this test.
    }
  }

  /**
   * A test {@link ProfilingDataProducer}.
   */
  private static final class TestProfilingDataProducer
      implements ProfilingDataProducer<ExtensionProfilingEventContext> {

    private ExtensionProfilingEventContext lastProfilingEventContext;

    @Override
    public void triggerProfilingEvent(ExtensionProfilingEventContext profilingEventContext) {
      this.lastProfilingEventContext = profilingEventContext;
    }

    public ExtensionProfilingEventContext getLastProfilingEventContext() {
      return lastProfilingEventContext;
    }
  }

}
