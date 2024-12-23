/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.mule.functional.junit4.matchers.MessageMatchers.hasPayload;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.test.http.functional.fips.DefaultTestConfiguration.isFipsTesting;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.test.http.functional.AbstractHttpTestCase;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.hamcrest.MatcherAssert;

public class HttpStandardRevocationConfigTestCase extends AbstractHttpTestCase {

  @Override
  protected String getConfigFile() {
    return "http-requester-standard-revocation-config.xml";
  }

  @BeforeClass
  public static void before() {
    assumeFalse("W-16968647: Check that this is not in fips where the standard revocation check does not work. Another of the documented options should be used",
                isFipsTesting());
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
    assertThat(event.getMessage(), hasPayload(both(containsString("test")).and(containsStringIgnoringCase("google"))));
  }
}
