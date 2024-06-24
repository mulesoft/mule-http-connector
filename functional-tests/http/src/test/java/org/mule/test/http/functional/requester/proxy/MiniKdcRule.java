/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.proxy;

import static java.lang.String.valueOf;

import java.io.File;
import java.util.Properties;

import org.apache.hadoop.minikdc.MiniKdc;
import org.junit.rules.ExternalResource;

public class MiniKdcRule extends ExternalResource {

  private final int kdcPort;
  private MiniKdc kdc;

  public MiniKdcRule(int kdcPort) {
    this.kdcPort = kdcPort;
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    File workDir = new File("target");
    Properties kdcConfig = createMiniKdcConf(valueOf(kdcPort));

    kdc = new MiniKdc(kdcConfig, workDir);
    kdc.start();
  }

  public synchronized void createPrincipal(File keytabFile, String... principals) throws Exception {
    kdc.createPrincipal(keytabFile, principals);
  }

  @Override
  protected void after() {
    if (kdc != null) {
      kdc.stop();
    }
    super.after();
  }

  private static Properties createMiniKdcConf(String kdcPort) {
    Properties conf = MiniKdc.createConf();
    conf.setProperty(MiniKdc.DEBUG, "true");
    conf.setProperty(MiniKdc.KDC_PORT, kdcPort);
    return conf;
  }
}
