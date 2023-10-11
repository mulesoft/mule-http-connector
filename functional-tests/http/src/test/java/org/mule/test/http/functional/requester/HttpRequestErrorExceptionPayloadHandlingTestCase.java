/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.ERRORS;
import static org.mule.test.http.functional.AllureConstants.HttpFeature.HttpStory.ERROR_HANDLING;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.functional.api.flow.FlowRunner;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.DynamicPort;

import io.qameta.allure.Issue;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;

@Stories({@Story(ERROR_HANDLING), @Story(ERRORS)})
@Issue("W-14067135")
public class HttpRequestErrorExceptionPayloadHandlingTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort unusedPort = new DynamicPort("unusedPort");


  @Override
  protected String getConfigFile() {
    return "http-request-errors-exception-payload-config.xml";
  }

  @Test
  public void connectivity() throws Exception {
    CoreEvent result = getFlowRunner("handled", unusedPort.getNumber()).run();
    assertThat(result.getMessage(),
               hasPayload(equalTo("<http:request config-ref=\"simpleConfig\" path=\"testPath\" responseTimeout=\"1000\">\n<http:headers><![CDATA[\n#[{'Content-Type': 'application/xml'}]\n]]></http:headers>\n</http:request>")));

  }

  private FlowRunner getFlowRunner(String flowName, int port) {
    return flowRunner(flowName).withVariable("port", port);
  }

}
