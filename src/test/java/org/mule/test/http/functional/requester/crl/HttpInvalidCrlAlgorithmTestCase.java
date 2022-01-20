/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.crl;

import static org.mule.runtime.core.api.extension.MuleExtensionModelProvider.getExtensionModel;
import static org.mule.runtime.core.api.extension.MuleExtensionModelProvider.getTlsExtensionModel;
import static org.mule.runtime.core.privileged.security.tls.TlsConfiguration.formatInvalidCrlAlgorithm;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.internal.temporary.HttpConnector;
import org.mule.extension.socket.api.SocketsExtension;
import org.mule.functional.junit4.AbstractConfigurationFailuresTestCase;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.CreateException;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.tck.junit4.rule.DynamicPort;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

public class HttpInvalidCrlAlgorithmTestCase extends AbstractConfigurationFailuresTestCase {

  @Rule
  public DynamicPort httpsPort = new DynamicPort("httpsPort");

  @Test
  public void testInvalidCrlAlgorithm() throws MuleException, InterruptedException {
    try {
      loadConfiguration("http-requester-tls-crl-invalid-algorithm-config.xml");
    } catch (Exception e) {
      Throwable rootCause = getRootCause(e);
      assertThat(rootCause, instanceOf(CreateException.class));
      assertThat(rootCause.getMessage(), is(formatInvalidCrlAlgorithm("SunX509")));
    }
  }

  @Override
  protected List<ExtensionModel> getRequiredExtensions() {
    ExtensionModel core = getExtensionModel();
    ExtensionModel tls = getTlsExtensionModel();
    ExtensionModel sockets = loadExtension(SocketsExtension.class, emptySet());
    ExtensionModel http = loadExtension(HttpConnector.class, singleton(sockets));
    return asList(http, sockets, tls, core);
  }

}
