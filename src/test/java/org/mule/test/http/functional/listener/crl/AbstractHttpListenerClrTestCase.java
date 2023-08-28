/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.crl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.api.error.HttpRequestFailedException;
import org.mule.test.http.functional.AbstractHttpTlsRevocationTestCase;

public abstract class AbstractHttpListenerClrTestCase extends AbstractHttpTlsRevocationTestCase {

  AbstractHttpListenerClrTestCase(String crlPath, String entityCertified) {
    super("http-listener-tls-revocation-file-config.xml", crlPath, entityCertified);
  }

  void verifyRemotelyClosedCause(Exception e) {
    assertThat(e.getCause(), instanceOf(HttpRequestFailedException.class));
    assertThat(e.getCause().getMessage(), containsString("Remotely closed"));
  }
}
