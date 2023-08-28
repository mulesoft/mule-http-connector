/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.net.HttpURLConnection;
import java.net.URL;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

public class HttpStandardRevocationConfigTestCase extends AbstractHttpTestCase {

  @Override
  protected String getConfigFile() {
    return "http-requester-standard-revocation-config.xml";
  }

  @Before
  public void setup() {
    Boolean reachable = false;
    try {
      URL url = new URL("http://www.google.com/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.connect();
      if (conn.getResponseCode() == 200) {
        reachable = true;
        conn.disconnect();
      }
    } catch (Exception e) {
      // ignore, already false by default
    }

    assumeTrue("Check for internet connection, and access to www.google.com", reachable);
  }

  @Test
  public void revocationWorksWithoutTrustStore() throws Exception {
    CoreEvent event = flowRunner("test").keepStreamsOpen().run();
    MatcherAssert.assertThat(((HttpResponseAttributes) event.getMessage().getAttributes().getValue()).getStatusCode(),
                             is(OK.getStatusCode()));
    assertThat(event.getMessage(), hasPayload(both(containsString("test")).and(containsString("google"))));
  }
}
